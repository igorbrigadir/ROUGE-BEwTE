package bewte.transforms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bewte.BE;

/**
 * Prepositions are mapped to all possible other prepositions that could mean the same thing
 *  using The Preposition Project data.
 */
public class PrepGeneralizer extends AbstractBETransform {
	
	public final static String PARAM_PREPOSITION_PROJECT_TAXONOMY_DIR = "PREPOSITION_TAXONOMY_DIR";
	
	private Map<String, Set<String>> mLemmaMap = new HashMap<String, Set<String>>();
	
	@Override
	public void initialize(Map<String, String> params) throws Exception {
		File dir = new File(PrepGeneralizer.class.getResource(params.get(PARAM_PREPOSITION_PROJECT_TAXONOMY_DIR)).getFile());
		System.out.println(PARAM_PREPOSITION_PROJECT_TAXONOMY_DIR + "=" + dir.toString());
		if(dir != null) {
			File[] files = dir.listFiles();
			for(File file : files) {
				if(!file.isDirectory()) {
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line = null;
					Set<String> lemmas = new HashSet<String>();
					while((line = reader.readLine()) != null) {
						line = line.trim();
						String[] split = line.split("\\t+");
						if(split.length > 1) {
							if(split[1].matches("[0-9]+=[0-9,]+")) {
								String lemma = split[0];
								lemmas.add(lemma);
							}
						}
					}
					for(String lemma : lemmas) {
						Set<String> mapsTo = mLemmaMap.get(lemma);
						if(mapsTo == null) {
							mLemmaMap.put(lemma, mapsTo = new HashSet<String>());
						}
						for(String lemma2 : lemmas) {
							if(!lemma.equals(lemma2)) {
								mapsTo.add(lemma2);
							}
						}
					}
					reader.close();
				}
			}		
		}
		else {
			System.err.println("Warning: preposition mapping data unavailable");
		}
	}
	
	@Override
	public List<BE> transform(BE be, Set<String> modelStrings) {
		List<BE> results = null;
		List<BE.BEPart> parts = be.getParts();
		if(parts.size() == 3) {
			BE.BEPart middlePart = parts.get(1);
			if(PREPOSITION.equals(middlePart.type) || TO.equals(middlePart.type)) {
				String prep = middlePart.text.toLowerCase();
				Set<String> transforms = mLemmaMap.get(prep);
				if(transforms != null && transforms.size() > 0) {
					results = new ArrayList<BE>(transforms.size()-1);
					for(String transform : transforms) {
						if(!transform.equals(prep)) {
							// no reason to inclue prepositions that never occur in the models
							if(modelStrings.contains(transform)) {
								List<BE.BEPart> newParts = new ArrayList<BE.BEPart>(parts);
								newParts.set(1, new BE.BEPart(transform, PREPOSITION));
								results.add(new BE(newParts, be.getRule(), be.getCoeff()));
							}
						}
					}
				}
			}
		}
		return results;
	}
	
}