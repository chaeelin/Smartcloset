package com.example.smartcloset.chat.service;

import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl implements PostService {

    @Override
    public void savePost(String content) {
        // 이 예제에서는 단순히 콘솔에 출력하는 방식으로 구현
        // 실제 애플리케이션에서는 데이터베이스에 저장하거나 다른 저장소에 저장할 수 있습니다.
        System.out.println("게시물 저장: " + content);
    }
}
