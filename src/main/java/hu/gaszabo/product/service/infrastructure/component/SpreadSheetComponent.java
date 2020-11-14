package hu.gaszabo.product.service.infrastructure.component;

import static hu.gaszabo.product.service.infrastructure.util.CollectionUtil.mapWithIndex;
import static hu.gaszabo.product.service.infrastructure.util.StreamUtil.sequentialStream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNoneBlank;
import static org.apache.poi.ss.usermodel.CellType.BLANK;
import static org.apache.poi.ss.usermodel.CellType.BOOLEAN;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.CellType.STRING;
import static org.springframework.util.Assert.isTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SpreadSheetComponent {

	private final ObjectMapper objectMapper;

	@Autowired
	public SpreadSheetComponent(final ObjectMapper objectMapper) {
		this.objectMapper = requireNonNull(objectMapper, "objectMapper can't be null");
	}

	public <T> List<T> processSheet(final MultipartFile file, final String sheetName, Class<T> rowType) {
		requireNonNull(file, "file can't be null");
		isTrue(isNoneBlank(sheetName), "sheetName can't be blank");
		requireNonNull(rowType, "rowType can't be null");

		Workbook workbook;
		try {
			workbook = new XSSFWorkbook(file.getInputStream());
		} catch (IOException e) {
			throw new IllegalStateException("Excel workbook couldn't be opened", e);
		}

		try {
			Sheet sheet = workbook.getSheet(sheetName);

			Row header = sheet.getRow(0);
			List<String> headerNames = //
					sequentialStream(header.spliterator()) //
							.map(c -> lowercase(c.getStringCellValue())) //
							.collect(toList());

			List<T> rows = sequentialStream(sheet.spliterator()) //
					.skip(1) // skip first line
					.map(mapTo(headerNames, rowType)) //
					.collect(toList());

			log.debug("Rows: {}", rows);

			return rows;
		} catch (Exception e) {
			throw new IllegalStateException("Excel workbook couldn't be closed", e);
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				throw new IllegalStateException("Excel workbook couldn't be closed", e);
			}
		}

	}

	public <T> InputStream appendSheet(final MultipartFile file, final String sheetName, List<T> items) {
		requireNonNull(file, "file can't be null");
		isTrue(isNoneBlank(sheetName), "sheetName can't be blank");
		requireNonNull(items, "items can't be null");

		Workbook workbook;
		try {
			workbook = new XSSFWorkbook(file.getInputStream());
		} catch (IOException e) {
			throw new IllegalStateException("Excel workbook couldn't be opened", e);
		}

		try {
			Sheet sheet = workbook.getSheet(sheetName);

			int last = sheet.getLastRowNum();
			List<Map<String, Object>> maps = items.stream().map(convertToMap()).collect(toList());

			long rows = mapWithIndex(maps.stream(), last + 1, createRows(sheet)).count();

			log.debug("Number of added rows: {}", rows);

			modifyFormula(sheet, last + rows + 1);
			XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());

		} catch (Exception e) {
			throw new IllegalStateException("Excel workbook couldn't be closed", e);
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				throw new IllegalStateException("Excel workbook couldn't be closed", e);
			}
		}

	}

	// -------------------------------------------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------------------------------------------

	private String lowercase(String header) {
		isTrue(isNoneBlank(header), "header can't be blank");
		return header.toLowerCase();
	}

	private <T> Function<Row, T> mapTo(final List<String> headerNames, final Class<T> rowType) {
		requireNonNull(headerNames, "headerNames can't be null");
		requireNonNull(rowType, "rowType can't be null");
		return row -> convertRow(row, headerNames, rowType);
	}

	private <T> T convertRow(final Row row, final List<String> headerNames, final Class<T> rowType) {
		requireNonNull(row, "row can't be null");
		requireNonNull(headerNames, "headerNames can't be null");
		requireNonNull(rowType, "rowType can't be null");

		Map<String, Object> map = new HashMap<>();
		sequentialStream(row.spliterator()) //
				.takeWhile(c -> c.getColumnIndex() < headerNames.size() - 1) //
				.forEach(convertCell(headerNames, map));

		return objectMapper.convertValue(map, rowType);
	}

	private Consumer<? super Cell> convertCell(final List<String> headerNames, final Map<String, Object> map) {
		requireNonNull(headerNames, "headerNames can't be null");
		requireNonNull(map, "map can't be null");

		return cell -> {
			CellType type = cell.getCellType();
			String headerName = headerNames.get(cell.getColumnIndex());
			switch (type) {
			case BLANK:
				map.put(headerName, "");
				break;
			case BOOLEAN:
				map.put(headerName, cell.getBooleanCellValue());
				break;
			case STRING:
				map.put(headerName, cell.getStringCellValue());
				break;
			case NUMERIC:
			case FORMULA:
				map.put(headerName, cell.getNumericCellValue());
				break;
			case ERROR:
				throw new IllegalStateException("Cell error");
			}
		};
	}

	private <T> Function<T, Map<String, Object>> convertToMap() {
		return i -> (Map<String, Object>) objectMapper.convertValue(i, Map.class);
	}

	private static BiFunction<Integer, Map<String, Object>, Row> createRows(final Sheet sheet) {
		requireNonNull(sheet, "sheet can't be null");
		return (index, map) -> {

			Row row = sheet.createRow(index);
			mapWithIndex(map.entrySet().stream(), createCells(row)).count();

			return row;
		};
	}

	private static BiFunction<Integer, Map.Entry<String, Object>, Cell> createCells(final Row row) {
		requireNonNull(row, "row can't be null");
		return (i, e) -> {
			Cell cell = row.createCell(i, determineCellTypeFrom(e.getValue()));
			addCellValue(cell, e.getValue());
			return cell;
		};
	}

	private static CellType determineCellTypeFrom(final Object value) {
		if (Boolean.class.isAssignableFrom(value.getClass())) {
			return BOOLEAN;
		} else if (String.class.isAssignableFrom(value.getClass())) {
			return STRING;
		} else if (Number.class.isAssignableFrom(value.getClass())) {
			return NUMERIC;
		}

		return BLANK;

	}

	private static void addCellValue(Cell cell, Object value) {
		requireNonNull(cell, "cell can't be null");

		if (Boolean.class.isAssignableFrom(value.getClass())) {
			cell.setCellValue((Boolean) value);
		} else if (String.class.isAssignableFrom(value.getClass())) {
			cell.setCellValue((String) value);
		} else if (Number.class.isAssignableFrom(value.getClass())) {
			cell.setCellValue((Long) value);
		}
	}

	private void modifyFormula(final Sheet sheet, long number) {
		requireNonNull(sheet, "sheet can't be null");
		sheet.getRow(1).getCell(5).setCellFormula("SUM(C2:C" + number + ")");
	}

}