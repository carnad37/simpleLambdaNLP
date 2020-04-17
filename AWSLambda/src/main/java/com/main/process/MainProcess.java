package com.main.process;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.openkoreantext.processor.KoreanTokenJava;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;

import com.amazonaws.services.lambda.runtime.Context; 
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.sub.vo.RequestVO;

import scala.collection.Seq;

public class MainProcess implements RequestHandler<RequestVO, String> {

	public String handleRequest(RequestVO request, Context context) {
				
		JSONObject resultObj = analysisSystem(request.getInput());
		
        return resultObj.toJSONString();
	}
	
	public JSONObject analysisSystem(String text) {
		
		JSONObject resultObj = new JSONObject();
		List<String> posList = new ArrayList<String>();
		posList.add("Noun");
		posList.add("Verb");
		posList.add("Adjective");
		
	    // 일반화
	    CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);

	    // 토큰화
	    Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);	    
	    List<KoreanTokenJava> koreanList = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens);

	    // 토큰체크
	    if (koreanList.size() < 1) {
	    	resultObj.put("result", "fail");
			return resultObj;
		}
	    
	    for (KoreanTokenJava koreanTokenJava : koreanList) {
	    	JSONObject unitKorean = null;
			String targetPos = koreanTokenJava.getPos().toString();
			
			//해당 형태소 포함여부 체크
			if (!posList.contains(targetPos)) continue;
			//어근화 가능한지 여부 확인
			String analysisWord = null;
			if (koreanTokenJava.getStem() != "") {
				analysisWord = koreanTokenJava.getStem();
			} else {
				analysisWord = koreanTokenJava.getText();
			}
			
			//해당 워드 포함여부 확인
			if (resultObj.containsKey(analysisWord)) {
				unitKorean = (JSONObject)resultObj.get(analysisWord);
				int count = (Integer)unitKorean.get("count") + 1;
				unitKorean.put("count", count);
			} else {
				unitKorean = new JSONObject();
				unitKorean.put("count", 1);
				unitKorean.put("pos", targetPos);
				resultObj.put(analysisWord, unitKorean);
			}			
		}	    
	    
	    return resultObj;
	    
	}

}
