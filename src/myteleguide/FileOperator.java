/**
 * 
 */
package myteleguide;

import java.io.BufferedInputStream;
import java.io.BufferedReader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

//import com.thaiopensource.relaxng.translate.Driver;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileOperator {
	private String url;
	private String outputGZFile; // cbilling.xmltv.xml.gz
	private String newGZFile; // cbilling.xmltv.xml.gz
	private String outputXMLFile;
	private String newOutputXMLFile;
	private String workingDir;
	private String idRemoveFile;

	private String getZipFileName() {
		return getWorkingDir() + getOutputGZFile();
	}

	private String getIdFileName() {
		return getWorkingDir() + "id.txt";
	}

	private String getIdRemoveFileName() {
		return getWorkingDir() + getIdRemoveFile();
	}

//	private String getDTDFileName() {
//		return getWorkingDir() + "test.dtd";
//	}

	private String getNewZipFileName() {
		return getWorkingDir() + getNewGZFile();
	}

	private String getXMLFileName() {
		return getWorkingDir() + getOutputXMLFile();
	}

	private String getNewXMLFileName() {
		return getWorkingDir() + getNewOutputXMLFile();
	}

	public boolean downloadFileFromUrl() {
		FileOutputStream fileOutputStream = null;
		try {
			BufferedInputStream in = new BufferedInputStream(new URI(url).toURL().openStream());
			fileOutputStream = new FileOutputStream(getZipFileName());
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				fileOutputStream.write(dataBuffer, 0, bytesRead);
			}
			fileOutputStream.close();
		} catch (IOException e) {
			return false;
		} catch (URISyntaxException e) {
			return false;
		}

		return true;
	}

	public boolean compressGzip() throws IOException {
		boolean result = false;
		Path source = Paths.get(getNewXMLFileName());
		Path target = Paths.get(getNewZipFileName());
		try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(target.toFile()));
				FileInputStream fis = new FileInputStream(source.toFile())) {

			// copy file
			byte[] buffer = new byte[1024];
			int len;
			while ((len = fis.read(buffer)) > 0) {
				gos.write(buffer, 0, len);
				result = true;
			}
		}
		return result;

	}

	public boolean decompressGzip() throws IOException {
		boolean result = false;
		Path source = Paths.get(getZipFileName());
		Path target = Paths.get(getXMLFileName());

		try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(source.toFile()));
				FileOutputStream fos = new FileOutputStream(target.toFile())) {

			// copy GZIPInputStream to FileOutputStream
			byte[] buffer = new byte[1024];
			int len;
			while ((len = gis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
				result = true;
			}
		}
		return result;
	}

	private boolean filter(String str, String prefix) {
		if (str.contains("-" + prefix + "-") || str.endsWith("-" + prefix) || str.startsWith(prefix + "-")) {
			return true;
		}
		return false;
	}

	private List<String> getRemoveChangelList() throws IOException {
		ArrayList<String> lstRemove = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(getIdRemoveFileName()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			lstRemove.add(line);
		}
		reader.close();
		return lstRemove;
	}

	public void createNewFile() throws IOException {
		FileWriter xmlWriter = new FileWriter(getNewXMLFileName());

		BufferedReader reader = new BufferedReader(new FileReader(getXMLFileName()));
		String line = null;
		ArrayList<String> lstAllowedChannels = new ArrayList<String>();
		ArrayList<String> lstProgramDates = new ArrayList<String>();
	
		boolean dontWrite = false;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("<channel id")) {
				String channelId = line.replace("<channel id=\"", "").replace("\">", "");
				String allowedChannel = filterChanels(channelId);
				if (allowedChannel != null) {
					lstAllowedChannels.add(allowedChannel);
					dontWrite = false;
				} else {
					dontWrite = true;
				}
			}
			if (line.startsWith("<programme start")) {
				String channelId = line.substring(line.indexOf("channel=")).replace("channel=\"", "").replace("\">",
						"");
				// <programme start="20230821
				String startDate = line.substring(18, 26);
				if (!lstProgramDates.contains(startDate)) {
					lstProgramDates.add(startDate);
				}
				if (lstAllowedChannels.contains(channelId)) {
					dontWrite = false;
				} else {
					dontWrite = true;
				}
			}
			if (!dontWrite) {
				xmlWriter.write(line + "\n");
			}

			if (dontWrite && (line.startsWith("</programme>") || line.startsWith("</channel>"))) {
				dontWrite = false;
			}

		}
		FileWriter idWritter = new FileWriter(getIdFileName());
		Collections.sort(lstAllowedChannels);
		lstAllowedChannels.stream().forEach(l -> {
			try {
				idWritter.write(l + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		idWritter.close();
		reader.close();
		xmlWriter.close();
		System.out.println("Program list for Days: ");
		lstProgramDates.forEach(l -> System.out.print(l+", "));
		// System.out.println(lstProgramDates.get(0) + " to: " +
		// lstProgramDates.get(lstProgramDates.size()-1));

	}

	private String filterChanels(String nodeValue) throws IOException {
		if (filter(nodeValue, "il"))
			return null;
		if (filter(nodeValue, "by"))
			return null;
		if (filter(nodeValue, "ua"))
			return null;
		if (filter(nodeValue, "uk"))
			return null;
		if (filter(nodeValue, "tr"))
			return null;
		if (filter(nodeValue, "lv"))
			return null;
		if (filter(nodeValue, "lt"))
			return null;
		if (filter(nodeValue, "ee"))
			return null;
		if (filter(nodeValue, "az"))
			return null;
		if (filter(nodeValue, "am"))
			return null;
		if (nodeValue.contains("futbol"))
			return null;
		if (filter(nodeValue, "plus"))
			return null;
		if (filter(nodeValue, "orig"))
			return null;

		if (filter(nodeValue, "igra"))
			return null;
		if (filter(nodeValue, "qazaqstan"))
			return null;

		List<String> removeChangelList = getRemoveChangelList();
		if (removeChangelList.contains(nodeValue))
			return null;

		return nodeValue;
	}

//	public void test() {
//		Driver d = new Driver();
//		String[] params = { getXMLFileName(), getXMLFileName(), getDTDFileName() };
//		d.run(params);
//	}

}
