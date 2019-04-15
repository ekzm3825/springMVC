package com.diquest.disa4.module.common.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 검색조건
 */
public class SearchCriteria extends PageRequest {

    @Getter
    @Setter
    protected String tablePartition;
    
    @Setter
    protected String orderByClause;
        
    @Getter
    protected List<Criteria> oredCriteria;

    public SearchCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected SearchCriteria(SearchCriteria criteria) {
        this.orderByClause = criteria.orderByClause;
        this.oredCriteria = criteria.oredCriteria;
        this.setPageNo(criteria.getPageNo());
        this.setPageSize(criteria.getPageSize());
    }

    public void add(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = new Criteria("OR");
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria and() {
        Criteria criteria = new Criteria("AND");
        oredCriteria.add(criteria);
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
    }

    public String getOrderByClause() {
        if (StringUtils.isEmpty(orderByClause))
            return null;

        List<String> ret = new ArrayList<>();
        String[] cols = orderByClause.split(",");
        for (String col : cols) {
            String clean = col.toUpperCase();
            if (clean.endsWith("ASC") || clean.endsWith("DESC")) {
                String[] keyVal = clean.trim().split("\\p{Space}");
                if (keyVal.length == 2) {
                    ret.add(keyVal[0] + " " + keyVal[1]);
                }
            }
        }

        return ret.size() == 0 ? null : StringUtils.join(ret, ", ");
    }

    public static class Criteria  {

        @Getter
        private String operator;

        @Getter
        private List<Criterion> criteria;

        public Criteria(String operator) {
            super();
            this.operator = operator;
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        protected void addCriterion(String operator, String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(operator, condition));
        }

        protected void addCriterion(String operator, String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(operator, condition, value));
        }

        protected void addCriterion(String operator, String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(operator, condition, value1, value2));
        }

        public Criteria orIsNull(String column) {
            addCriterion("OR", column + " IS NULL");
            return this;
        }

        public Criteria orIsNotNull(String column) {
            addCriterion("OR", column + " IS NOT NULL");
            return this;
        }

        public Criteria orEqualTo(String column, Object value) {
            addCriterion("OR", column + " =", value, column);
            return this;
        }

        public Criteria orNotEqualTo(String column, Object value) {
            addCriterion("OR", column + " <>", value, column);
            return this;
        }

        public Criteria orGreaterThan(String column, Object value) {
            addCriterion("OR", column + " >", value, column);
            return this;
        }

        public Criteria orGreaterThanOrEqualTo(String column, Object value) {
            addCriterion("OR", column + " >=", value, column);
            return this;
        }

        public Criteria orLessThan(String column, Object value) {
            addCriterion("OR", column + " <", value, column);
            return this;
        }

        public Criteria orLessThanOrEqualTo(String column, Object value) {
            addCriterion("OR", column + " <=", value, column);
            return this;
        }

        public Criteria orLike(String column, Object value) {
            addCriterion("OR", column + " LIKE", value, column);
            return this;
        }

        public Criteria orLikeLeft(String column, Object value) {
            addCriterion("OR", column + " LIKE", "%" + value, column);
            return this;
        }

        public Criteria orLikeRight(String column, Object value) {
            addCriterion("OR", column + " LIKE", value + "%", column);
            return this;
        }

        public Criteria orLikeBoth(String column, Object value) {
            addCriterion("OR", column + " LIKE", "%" + value + "%", column);
            return this;
        }

        public Criteria orNotLike(String column, Object value) {
            addCriterion("OR", column + " NOT LIKE", value, column);
            return this;
        }

        public Criteria orIn(String column, List<? extends Object> values) {
            addCriterion("OR", column + " IN", values, column);
            return this;
        }

        public Criteria orNotIn(String column, List<? extends Object> values) {
            addCriterion("OR", column + " NOT IN", values, column);
            return this;
        }

        public Criteria orBetween(String column, Object value1, Object value2) {
            addCriterion("OR", column + " BETWEEN", value1, value2, column);
            return this;
        }

        public Criteria orNotBetween(String column, Object value1, Object value2) {
            addCriterion("OR", column + " NOT BETWEEN", value1, value2, column);
            return this;
        }

        public Criteria orDateRange(String column, Object value1, Object value2) {
            addCriterion("OR", column + " BETWEEN", value1, value2, column);
            return this;
        }
        
        public Criteria andIsNull(String column) {
            addCriterion("AND", column + " IS NULL");
            return this;
        }

        public Criteria andIsNotNull(String column) {
            addCriterion("AND", column + " IS NOT NULL");
            return this;
        }

        public Criteria andEqualTo(String column, Object value) {
            addCriterion("AND", column + " =", value, column);
            return this;
        }

        public Criteria andNotEqualTo(String column, Object value) {
            addCriterion("AND", column + " <>", value, column);
            return this;
        }

        public Criteria andGreaterThan(String column, Object value) {
            addCriterion("AND", column + " >", value, column);
            return this;
        }

        public Criteria andGreaterThanOrEqualTo(String column, Object value) {
            addCriterion("AND", column + " >=", value, column);
            return this;
        }

        public Criteria andLessThan(String column, Object value) {
            addCriterion("AND", column + " <", value, column);
            return this;
        }

        public Criteria andLessThanOrEqualTo(String column, Object value) {
            addCriterion("AND", column + " <=", value, column);
            return this;
        }

        public Criteria andLike(String column, Object value) {
            addCriterion("AND", column + " LIKE", value, column);
            return this;
        }

        public Criteria andLikeLeft(String column, Object value) {
            addCriterion("AND", column + " LIKE", "%" + value, column);
            return this;
        }

        public Criteria andLikeRight(String column, Object value) {
            addCriterion("AND", column + " LIKE", value + "%", column);
            return this;
        }

        public Criteria andLikeBoth(String column, Object value) {
            addCriterion("AND", column + " LIKE", "%" + value + "%", column);
            return this;
        }

        public Criteria andNotLike(String column, Object value) {
            addCriterion("AND", column + " NOT LIKE", value, column);
            return this;
        }

        public Criteria andIn(String column, List<? extends Object> values) {
            addCriterion("AND", column + " IN", values, column);
            return this;
        }

        public Criteria andNotIn(String column, List<? extends Object> values) {
            addCriterion("AND", column + " NOT IN", values, column);
            return this;
        }

        public Criteria andBetween(String column, Object value1, Object value2) {
            addCriterion("AND", column + " BETWEEN", value1, value2, column);
            return this;
        }

        public Criteria andNotBetween(String column, Object value1, Object value2) {
            addCriterion("AND", column + " NOT BETWEEN", value1, value2, column);
            return this;
        }

        public Criteria andDateRange(String column, Object value1, Object value2) {
            addCriterion("AND", column + " BETWEEN", value1, value2, column);
            return this;
        }
        
    }

    public static class Criterion {

        @Getter
        private String operator;

        @Getter
        private String condition;

        @Getter
        private Object value;

        @Getter
        private Object secondValue;

        @Getter
        private boolean noValue;

        @Getter
        private boolean singleValue;

        @Getter
        private boolean betweenValue;

        @Getter
        private boolean dateRangeValue;

        @Getter
        private boolean listValue;

        @Getter
        private String typeHandler;

        protected Criterion(String operator, String condition) {
            super();
            this.operator = operator;
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String operator, String condition, Object value, String typeHandler) {
            super();
            this.operator = operator;
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String operator, String condition, Object value) {
            this(operator, condition, value, null);
        }

        protected Criterion(String operator, String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.operator = operator;
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String operator, String condition, Object value, Object secondValue) {
            this(operator, condition, value, secondValue, null);
        }

        protected Criterion(String operator, String condition, Date value, Date secondValue, String typeHandler) {
            super();
            this.operator = operator;
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.dateRangeValue = true;
        }

        protected Criterion(String operator, String condition, Date value, Date secondValue) {
            this(operator, condition, value, secondValue, null);
        }
    }

}