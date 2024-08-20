package com.example.smartcloset.chat.util;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class HashTagGenerator {

    // 강조된 부분을 해시태그로 변환하는 메서드
    public String generateHashTagsFromBoldText(String text) {
        // **로 감싸진 텍스트를 추출하기 위한 정규 표현식
        Pattern pattern = Pattern.compile("\\*\\*(.*?)\\*\\*");
        Matcher matcher = pattern.matcher(text);

        Set<String> hashtags = new HashSet<>();
        while (matcher.find()) {
            // 강조된 텍스트 추출
            String boldText = matcher.group(1).trim();
            // 해시태그 형식으로 변환 및 +로 구분된 부분 처리
            hashtags.addAll(Arrays.stream(boldText.split("\\+"))
                    .map(part -> "#" + part.trim().replaceAll("[^a-zA-Z가-힣]", "").replaceAll("\\s+", ""))
                    .collect(Collectors.toSet()));
        }

        // 해시태그를 문자열로 결합
        return String.join(" ", hashtags);
    }
}
