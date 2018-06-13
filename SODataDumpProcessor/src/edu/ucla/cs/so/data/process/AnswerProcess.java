package edu.ucla.cs.so.data.process;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import edu.ucla.cs.so.data.model.Question;
import edu.ucla.cs.so.mysql.utils.MySQLAccess;

public class AnswerProcess {
	private static final int MAX_ENTRIES = 20000;

	public static void main(String[] args) throws FileNotFoundException,
			XMLStreamException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLStreamReader reader = inputFactory
				.createXMLStreamReader(new FileInputStream(
						"/media/troy/Disk2/research/data/StackOverflow Dump/Posts.xml"));

		MySQLAccess access = new MySQLAccess();
		access.connect();
		
		// add cache
		LinkedHashMap<String, Question> cache = new LinkedHashMap<String, Question>(MAX_ENTRIES + 1, 1.1F, true) {
			protected boolean removeEldestEntry(Map.Entry eldest) {
				   return size() > MAX_ENTRIES;
			}
		};

		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLEvent.START_ELEMENT
					&& reader.getLocalName().equals("row")) {
				String id = reader.getAttributeValue(null, "Id");
//				int iid = Integer.parseInt(id);
//				if(iid <= 37188190) {
//					// resume from the last parse exception
//					continue;
//				}
				
				String postTypeId = reader
						.getAttributeValue(null, "PostTypeId");
				if (postTypeId.equals("2")) {
					// process answer posts
					String parentId = reader
							.getAttributeValue(null, "ParentId");
					// check whether this post is associated with a Java
					// or Android question
					Question q;
					if(cache.containsKey(parentId)) {
						q = cache.get(parentId);
					} else {
						// query the database
						q = access.selectQuestionPosts(parentId);
						// update the cache
						cache.put(parentId, q);
					}
					
					if (q == null) {
						// this is not an answer related to Java or Android
						continue;
					}
					
					if(q.tags.contains("<java>")) {
						// this answer post has already been processed and stored in the database
						continue;
					}

					String accepted = "0";
					if (id.equals(q.accepted)) {
						accepted = "1";
					}

					String body = reader.getAttributeValue(null, "Body");
					
					if(!body.contains("<code>")) {
						// we are only interested in answer posts with code snippets
						continue;
					}
					
					String score = reader.getAttributeValue(null, "Score");

					access.insertAnswerPost(id, parentId, body, score,
							accepted, q.tags, q.view);
					System.out.println(id + "**" + parentId
							+ "** Ignore Body **" + score + "**" + accepted
							+ "**" + q.tags + "**" + q.view);
				}
			}
		}
		access.close();
		reader.close();
	}
}
