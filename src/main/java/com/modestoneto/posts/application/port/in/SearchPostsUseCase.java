package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.application.common.PageRequest;
import com.modestoneto.posts.application.common.PageResult;
import com.modestoneto.posts.domain.model.Post;
import java.time.LocalDate;

public interface SearchPostsUseCase {

    // text, minDate e maxDate são opcionais; as datas incluem o dia inteiro, em UTC
    PageResult<Post> search(String text, LocalDate minDate, LocalDate maxDate, PageRequest request);
}
