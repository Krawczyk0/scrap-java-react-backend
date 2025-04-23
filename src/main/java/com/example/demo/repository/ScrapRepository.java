package com.example.demo.repository;

import com.example.demo.model.ScrapModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapRepository extends MongoRepository <ScrapModel,String>{
    boolean existsByTitle(String title);
}