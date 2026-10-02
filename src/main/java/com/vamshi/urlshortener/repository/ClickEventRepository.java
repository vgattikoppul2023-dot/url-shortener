package com.vamshi.urlshortener.repository;

import com.vamshi.urlshortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    List<ClickEvent> findTop10ByShortCodeOrderByClickedAtDesc(String shortCode);
}
