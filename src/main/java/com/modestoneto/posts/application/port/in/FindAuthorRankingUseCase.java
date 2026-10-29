package com.modestoneto.posts.application.port.in;

import com.modestoneto.posts.domain.model.AuthorRanking;
import java.util.List;

public interface FindAuthorRankingUseCase {

    List<AuthorRanking> ranking(int limit);
}
