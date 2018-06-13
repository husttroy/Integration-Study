package edu.ucla.cs.so.data.process;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import edu.ucla.cs.so.mysql.utils.MySQLAccess;

public class QuestionProcess {
	
	public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader reader = inputFactory.createXMLStreamReader(new FileInputStream("/media/troy/Disk2/research/data/StackOverflow Dump/Posts.xml"));
		
		MySQLAccess access = new MySQLAccess();
		access.connect();
		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLEvent.START_ELEMENT && reader.getLocalName().equals("row")) {
				String id = reader.getAttributeValue(null, "Id");
//				int iid = Integer.parseInt(id);
//				if(iid <= 36968862) {
//					// resume from the last parse exception
//					continue;
//				}
				String postTypeId = reader.getAttributeValue(null, "PostTypeId");
				if(postTypeId.equals("1")) {
					// process question posts
					String tags = reader.getAttributeValue(null, "Tags");
					if(tags != null && (tags.contains("<java>") || tags.contains("<android>"))) {
						// find out SO questions in Java or Android
						String accepted = reader.getAttributeValue(null, "AcceptedAnswerId");
						String viewCount = reader.getAttributeValue(null, "ViewCount");
						access.insertQuestionPost(id, accepted, viewCount, tags);
						System.out.println(id + "**" + accepted + "**" + viewCount + "**" + tags);
					}
				}
			}
		}
		access.close();
		reader.close();
	}
}
