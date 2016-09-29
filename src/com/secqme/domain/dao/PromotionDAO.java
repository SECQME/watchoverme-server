package com.secqme.domain.dao;

import com.secqme.domain.model.promotion.PromotionVO;

import java.util.List;

/**
 * User: James Khoo
 * Date: 12/9/13
 * Time: 2:23 PM
 */
public interface PromotionDAO extends BaseDAO<PromotionVO, String> {
    public List<PromotionVO> findAll();
}
