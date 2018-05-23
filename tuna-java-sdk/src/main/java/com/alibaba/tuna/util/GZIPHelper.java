package com.alibaba.tuna.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPHelper {
	public static byte[] zip(byte[] value) throws IOException {
		ByteArrayOutputStream output = null;
		GZIPOutputStream zip = null;
		try {
			output = new ByteArrayOutputStream();
			zip = new GZIPOutputStream(output);
			zip.write(value, 0, value.length);
			zip.close();
			return output.toByteArray();
		} finally {
			if (zip != null)
				zip.close();
			if (output != null)
				output.close();
		}
	}

	public static byte[] unzip(byte[] value) throws IOException {
		ByteArrayOutputStream output = null;
		ByteArrayInputStream input = null;
		GZIPInputStream zip = null;
		try {
			output = new ByteArrayOutputStream();
			input = new ByteArrayInputStream(value);
			zip = new GZIPInputStream(input);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = zip.read(buffer)) > 0)
				output.write(buffer, 0, read);
			return output.toByteArray();
		} finally {
			if (zip != null)
				zip.close();
			if (output != null)
				output.close();
			if (input != null)
				input.close();
		}
	}
}
