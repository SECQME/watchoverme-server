package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.PromotionDAO;
import com.secqme.domain.model.promotion.PromotionVO;

import java.util.List;

/**
 * User: James Khoo
 * Date: 12/9/13
 * Time: 2:25 PM
 */
public class PromotionJPADAO extends BaseJPADAO<PromotionVO, String> implements PromotionDAO {
    public PromotionJPADAO() {
        super(PromotionVO.class);
    }

    public List<PromotionVO> findAll() {
         return this.executeQueryWithResultList(PromotionVO.QUERY_FIND_ALL);
    }
}
