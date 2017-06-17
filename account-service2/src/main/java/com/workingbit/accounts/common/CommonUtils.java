package com.workingbit.accounts.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class CommonUtils {

	/**
	 * Convert a JSON string to a map
	 *
	 * @param json
	 * @return
	 */
	public static Map<String, Object> convertJSONtoMap(String json) {
		return convertJSONtoMap(json, false);
	}

	/**
	 * Convert a JSON string to a map
	 *
	 * @param json
	 * @param addQueryData
	 * @return
	 */
	public static StringMap convertJSONtoMap(String json, boolean addQueryData) {

		if (StringUtils.isBlank(json))
			return StringMap.emptyMap();

		Map<String, Object> map = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		try {
			map = mapper.readValue(json, new TypeReference<HashMap<String, Object>>() {});
			cleanseMap(map);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return StringMap.fromMap(map);
	}

	/**
	 * Convert a JSON input to a list of map
	 *
	 * @param json
	 * @return
	 */
	public static List<Map<String, Object>> convertJSONtoList(String json) {

		if (StringUtils.isBlank(json))
			return new ArrayList<>();

		List<Map<String, Object>> list = new ArrayList<>();
		ObjectMapper mapper = new ObjectMapper();

		try {
			list = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Convert a map to a JSON string
	 *
	 * @param map
	 * @return
	 */
	public static String convertMapToJSON(Map<String, Object> map) {
		cleanseMap(map);
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(map);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Convert a list of Map to a JSON string
	 *
	 * @param list
	 * @return
	 */
	public static String convertListToJSON(List<Map<String, Object>> list) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(list);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Clear NULL and empty fields from MAP
	 * @param map
	 */
	public static Map<String, Object> cleanseMap(Map<String, Object> map) {
		Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
		List<String> keys = new ArrayList<String>();
		while(iterator.hasNext()){
			String key = iterator.next().getKey();
			if(map.get(key)==null || map.get(key).toString().trim().isEmpty())
				keys.add(key);
		}
		if(!keys.isEmpty())
			for(String key : keys)
				map.remove(key);
		return map;
	}


	public static int getUserAge(String birthday) {
		int age = 0;
		if (!StringUtils.isBlank(birthday)) {
			final String[] birthdayArr = birthday.split("/");

			final LocalDate now = LocalDate.now();
			if (birthdayArr.length == 3) {
				final Integer month = Integer.valueOf(birthdayArr[0]);
				final Integer day = Integer.valueOf(birthdayArr[1]);
				final Integer year = Integer.valueOf(birthdayArr[2]);
				age = now.getYear() - year + (now.getMonth().getValue() < month ? -1 : now.getDayOfMonth() < day ? -1 : 0);
			} else if (birthdayArr.length == 2) {
				age = 0;
			} else {
				age = now.getYear() - Integer.valueOf(birthdayArr[0]);
			}
		}
		return age;
	}

	public static Object formatDate(Object birthday) {
		return birthday;
	}
}
