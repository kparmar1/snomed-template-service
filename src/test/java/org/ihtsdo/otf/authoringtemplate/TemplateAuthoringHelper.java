package org.ihtsdo.otf.authoringtemplate;

import com.google.common.base.CaseFormat;
import com.google.common.io.Files;
import org.junit.Assert;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * This is not a unit test.
 * Class help with the manual creation of the logical part of templates.
 */
public class TemplateAuthoringHelper {

	public static void main(String[] args) throws IOException {
		readInput();
//		compactScratch();
//		expandScratch();
	}

	// Helper code for converting template specification tables into template language!
	// Not for production use
	private static void readInput() throws IOException {
		File file = new File("snomed-templates/.input.txt");

		List<String> lines = Files.readLines(file, Charset.forName("UTF-8"));

		String type = null;
		List<String> range = new ArrayList<>();
		String group = null;
		String lastGroup = "";
		boolean slot = false;
		String template = "";

		for (String line : lines) {
			line = line.trim();
			if (type == null) {
				type = line;
			} else {
				if (line.length() > 2) {
					range.add(line);
					if (line.contains("<")) {
						slot = true;
					}
				} else {
					group = line;
					if (range.size() > 1) {
						slot = true;
					}
					if (type.contains("Is a (attribute)")) {
						Assert.assertEquals(1, range.size());
						template = range.get(0) + ":\n";
						group = null;
					} else {
						if (!group.equals(lastGroup)) {
							if (!group.equals("0")) {
								template += "\t},\n";
							}
							template += "\t{\n";
						} else {
							template += ",\n";
						}
						template += "\t\t" + type + " = ";
						if (slot) {
							String slotName = type.substring(type.indexOf("|") + 1, type.indexOf("(")).replace(" ", "_");
							slotName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, slotName);
							template += "[[+id(";
							for (int i = 0; i < range.size(); i++) {
								String s = range.get(i);
								s = s.replaceAll("<[ ]*", "<");
								if (i > 0) {
									template += " OR ";
								}
								template += s;
							}
							template += ") @" + slotName + "]]";
						}
					}
					type = null;
					range.clear();
					lastGroup = group;
					group = null;
					slot = false;
				}
			}
		}
		template += "\n\t}\n";
		System.out.println(template);
		System.out.println();
		System.out.println(template.replace("\t", "\\t").replace("\n", "\\n"));
	}

	private static void compactScratch() throws IOException {transformScratch(true);}

	private static void expandScratch() throws IOException {transformScratch(false);}

	private static void transformScratch(boolean compact) throws IOException {
		File file = new File("snomed-templates/.scratch.txt");
		File tempFile = new File(file.getAbsolutePath() + ".tmp");

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (compact) {
						line = line.replace("\t", "\\t");
						line = line.replace("\n", "\\n");
						line += "\\n";
						writer.write(line);
					} else {
						line = line.replace("\\t", "\t");
						line = line.replace("\\n", "\n");
						writer.write(line);
						writer.newLine();
					}
				}
			}
		}
		Files.move(tempFile, file);
	}

}