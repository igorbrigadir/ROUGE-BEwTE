/*
 * Copyright 2011 University of Southern California 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package tratz.parse.transform;

import tratz.parse.types.Parse;

public class BasicStanfordTransformer implements ParseTransform {
	
	private VchTransformer mVchTransformer = new VchTransformer();
	private CcTransformer mCcTransformer = new CcTransformer();
	private StanNameTransformer mNameTransformer = new StanNameTransformer();
	private ObjcompTransformer mObjcompTransformer = new ObjcompTransformer();
	private CopTransformer mCopTransformer = new CopTransformer();
	private NumberTransformer mNumTransformer = new NumberTransformer();
	private PseudoPrepTransformer mPseudoPrepTransformer = new PseudoPrepTransformer();
	
	@Override
	public void performTransformation(Parse parse) {
		mVchTransformer.performTransformation(parse);
		mCcTransformer.performTransformation(parse);
		mNameTransformer.performTransformation(parse);
		mObjcompTransformer.performTransformation(parse);
		mCopTransformer.performTransformation(parse);
		mNumTransformer.performTransformation(parse);
		mPseudoPrepTransformer.performTransformation(parse);
	}
}