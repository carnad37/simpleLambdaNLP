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
		
	    // �Ϲ�ȭ
	    CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);

	    // ��ūȭ
	    Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);	    
	    List<KoreanTokenJava> koreanList = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens);

	    // ��ūüũ
	    if (koreanList.size() < 1) {
	    	resultObj.put("result", "fail");
			return resultObj;
		}
	    
	    for (KoreanTokenJava koreanTokenJava : koreanList) {
	    	JSONObject unitKorean = null;
			String targetPos = koreanTokenJava.getPos().toString();
			
			//�ش� ���¼� ���Կ��� üũ
			if (!posList.contains(targetPos)) continue;
			//���ȭ �������� ���� Ȯ��
			String analysisWord = null;
			if (koreanTokenJava.getStem() != "") {
				analysisWord = koreanTokenJava.getStem();
			} else {
				analysisWord = koreanTokenJava.getText();
			}
			
			//�ش� ���� ���Կ��� Ȯ��
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
