package myteleguide;

public class Starter {
	private final static String URL = "http://epg.g-cdn.app/xmltv.xml.gz";
	private final static String FILE_ORIGINAL_GZ = "orig_xmltv.xml.gz";
	private final static String FILE_ORIGINAL_XML = "orig_cbilling.xmltv.xml";
	private final static String FILE_OUTPUT_NEW_GZ = "xmltv.xml.gz";
	private final static String FILE_OUTPUT_XML = "cbilling.xmltv.xml";
	private final static String WORKING_DIR = "C:/Users/HomePC/git/repository/myteleguide/output/";
	private final static String FILE_REMOVE_CHANNELS_TXT = "idremove.txt";

	public static void main(String[] args) throws Exception {
		FileOperator fileDownloader = FileOperator.builder() //
				.workingDir(WORKING_DIR) //
				.url(URL)//
				.outputGZFile(FILE_ORIGINAL_GZ)//
				.outputXMLFile(FILE_ORIGINAL_XML)//
				.newGZFile(FILE_OUTPUT_NEW_GZ)//
				.newOutputXMLFile(FILE_OUTPUT_XML)//
				.idRemoveFile(FILE_REMOVE_CHANNELS_TXT)//
				.build();

		boolean extractOnly = true;
		boolean doZip = true;

		if (!extractOnly) {
			boolean downloadFileFromUrl = fileDownloader.downloadFileFromUrl();
			System.out.println("File Downloaded:" + downloadFileFromUrl);
			boolean uzipFile = fileDownloader.decompressGzip();
			System.out.println("unzip: " + uzipFile);
		}
		System.out.println("start process");
		fileDownloader.createNewFile();
		System.out.println("end process");
		if (doZip) {
			boolean compressGzip = fileDownloader.compressGzip();
			System.out.println("zip file: " + compressGzip);
		}
	}

}
