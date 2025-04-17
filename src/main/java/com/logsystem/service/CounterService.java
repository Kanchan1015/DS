package com.logsystem.service;

import com.logsystem.model.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class CounterService {

    private final MongoOperations mongoOperations;

    @Autowired
    public CounterService(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    public int getNextSequence(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().inc("seq", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);
        Counter counter = mongoOperations.findAndModify(query, update, options, Counter.class);
        return counter != null ? counter.getSeq() : 1;
    }
}