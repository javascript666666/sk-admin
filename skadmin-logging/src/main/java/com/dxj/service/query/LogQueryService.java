package com.dxj.service.query;

import com.dxj.domain.Log;
import com.dxj.domain.LoginLog;
import com.dxj.repository.LogRepository;
import com.dxj.service.mapper.LogErrorMapper;
import com.dxj.service.mapper.LogSmallMapper;
import com.dxj.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dxj
 * @date 2018-11-24
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class LogQueryService {

    private final LogRepository logRepository;

    private final LogErrorMapper logErrorMapper;

    private final LogSmallMapper logSmallMapper;

    @Autowired
    public LogQueryService(LogRepository logRepository, LogErrorMapper logErrorMapper, LogSmallMapper logSmallMapper) {
        this.logRepository = logRepository;
        this.logErrorMapper = logErrorMapper;
        this.logSmallMapper = logSmallMapper;
    }

    public Object queryAll(Log log, Pageable pageable){
        Page<Log> page = logRepository.findAll(new Spec(log),pageable);
        if (!ObjectUtils.isEmpty(log.getUsername())) {
            return PageUtil.toPage(page.map(logSmallMapper::toDto));
        }
        if (log.getLogType().equals("ERROR")) {
            return PageUtil.toPage(page.map(logErrorMapper::toDto));
        }
        return logRepository.findAll(new Spec(log),pageable);
    }

    class Spec implements Specification<Log> {

        private Log log;

        Spec(Log log){
            this.log = log;
        }

        @Override
        public Predicate toPredicate(Root<Log> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {

            List<Predicate> list = new ArrayList<Predicate>();


            if(!ObjectUtils.isEmpty(log.getUsername())){
                list.add(cb.like(root.get("username").as(String.class),"%"+log.getUsername()+"%"));
            }

            if (!ObjectUtils.isEmpty(log.getLogType())) {
                list.add(cb.equal(root.get("logType").as(String.class), log.getLogType()));
            }

            Predicate[] p = new Predicate[list.size()];
            return cb.and(list.toArray(p));
        }
    }
}