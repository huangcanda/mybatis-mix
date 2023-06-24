package org.wanghailu.mybatismix.mapper.jpaquery;


import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.wanghailu.mybatismix.constant.UpdateModeEnum;
import org.wanghailu.mybatismix.provider.SqlProviderHelper;
import org.wanghailu.mybatismix.support.TwoTuple;
import org.wanghailu.mybatismix.util.EntityUtils;
import org.wanghailu.mybatismix.util.PrivateStringUtils;
import org.wanghailu.mybatismix.util.TruckUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * 解析方法名，修改自org.springframework.data.repository.query.parser.PartTree
 */
@SuppressWarnings("ALL")
public class PartTree {
    
    private static String KEYWORD_TEMPLATE = "(%s)(?=(\\p{Lu}|\\P{InBASIC_LATIN}))";
    
    private static String QUERY_PATTERN = "find|read|get|query|select|search|stream";
    
    private static String COUNT_PATTERN = "count";
    
    private static String EXISTS_PATTERN = "exists";
    
    private static String DELETE_PATTERN = "delete|remove";
    
    private static String UPDATE_PATTERN = "update|modify";
    
    private static Pattern PREFIX_TEMPLATE = Pattern.compile(
            "^(" + QUERY_PATTERN + "|" + COUNT_PATTERN + "|" + EXISTS_PATTERN + "|" + DELETE_PATTERN + "|"
                    + UPDATE_PATTERN + ")((\\p{Lu}.*?))??By");
    
    private static String DISTINCT = "Distinct";
    
    private static Pattern COUNT_BY_TEMPLATE = Pattern.compile("^count(\\p{Lu}.*?)??By");
    
    private static Pattern EXISTS_BY_TEMPLATE = Pattern.compile("^(" + EXISTS_PATTERN + ")(\\p{Lu}.*?)??By");
    
    private static Pattern DELETE_BY_TEMPLATE = Pattern.compile("^(" + DELETE_PATTERN + ")(\\p{Lu}.*?)??By");
    
    private static Pattern UPDATE_BY_TEMPLATE = Pattern.compile("^(" + UPDATE_PATTERN + ")(\\p{Lu}.*?)??By");
    
    private static String LIMITING_QUERY_PATTERN = "(First|Top)(\\d*)?";
    
    private static Pattern LIMITED_QUERY_TEMPLATE = Pattern
            .compile("^(" + QUERY_PATTERN + ")(" + DISTINCT + ")?" + LIMITING_QUERY_PATTERN + "(\\p{Lu}.*?)??By");
    
    
    private String source;
    
    private Class<?> domainClass;
    
    /**
     * The subject, for example "findDistinctUserByNameOrderByAge" would have the subject "DistinctUser".
     */
    private Subject subject;
    
    /**
     * The subject, for example "findDistinctUserByNameOrderByAge" would have the predicate "Name".
     */
    private List<OrPart> whereCondition;
    
    /**
     * The subject, for example "findDistinctUserByNameOrderByAge" would have the OrderBySource "OrderByAge".
     */
    private OrderBySource orderBySource;
    
    private int argIndex = 1;
    
    public PartTree(String source, Class<?> domainClass) {
        this.source = source;
        this.domainClass = domainClass;
        Matcher matcher = PREFIX_TEMPLATE.matcher(source);
        String subject;
        String predicate;
        if (matcher.find()) {
            subject = matcher.group(0);
            predicate = source.substring(matcher.group().length());
        } else {
            subject = "";
            predicate = source;
        }
        this.subject = new Subject(subject);
        String[] parts = split(predicate, "OrderBy");
        if (parts.length > 2) {
            throw new IllegalArgumentException("OrderBy must not be used more than once in a method name!");
        }
        this.whereCondition = Arrays.stream(split(parts[0], "Or")).filter(PrivateStringUtils::isNotBlank)
                .map(OrPart::new).collect(Collectors.toList());
        String orderByClause = parts.length == 2 ? parts[1] : null;
        this.orderBySource = new OrderBySource(orderByClause);
    }
    
    public SqlCommandType getSqlCommandType() {
        if (subject.delete) {
            return SqlCommandType.DELETE;
        } else if (subject.updateMode != null) {
            return SqlCommandType.UPDATE;
        } else {
            return SqlCommandType.SELECT;
        }
    }
    
    public Integer getLimit() {
        return subject.maxResults;
    }
    
    public String getSource() {
        return source;
    }
    
    public Class<?> getDomainClass() {
        return domainClass;
    }
    
    public String getSql(MapperMethodArgsMap argsMap) {
        SqlCommandType sqlCommandType = getSqlCommandType();
        StringBuilder sqlBuilder = new StringBuilder(50);
        if (sqlCommandType == SqlCommandType.UPDATE) {
            sqlBuilder.append("UPDATE ");
            sqlBuilder.append(EntityUtils.getTableName(domainClass));
            sqlBuilder.append(" SET ");
            
            Object entity = argsMap.get(1);
            argsMap.putMap("setEntity", entity);
            TruckUtils.assertNotNull(entity, "第一个参数为entity，表示set内容，不能为空");
            sqlBuilder.append(SqlProviderHelper
                    .getUpdateSetSqlByUpdateMode(entity, subject.updateMode.getValue(), domainClass,
                            ParamNameResolver.GENERIC_NAME_PREFIX + "1."));
            sqlBuilder.append(" ");
        } else if (sqlCommandType == SqlCommandType.DELETE) {
            sqlBuilder.append("DELETE FROM ");
            sqlBuilder.append(EntityUtils.getTableName(domainClass));
        } else {
            sqlBuilder.append(getSelectStr());
            sqlBuilder.append("FROM ");
            sqlBuilder.append(EntityUtils.getTableName(domainClass));
        }
        sqlBuilder.append(getWhereConditionStr(argsMap));
        sqlBuilder.append(orderBySource);
        return sqlBuilder.toString();
    }
    
    public String getSelectStr() {
        StringBuilder sqlBuilder = new StringBuilder("SELECT ");
        if (subject.count) {
            sqlBuilder.append("COUNT(*) ");
        } else {
            if (subject.distinct) {
                sqlBuilder.append("DISTINCT ");
            }
            if (subject.exists) {
                sqlBuilder.append(EntityUtils.getPrimaryKeyColumnName(domainClass));
            } else if (TruckUtils.isEmpty(subject.selectItem)) {
                sqlBuilder.append(EntityUtils.getSelectAllColumnName(domainClass));
            } else {
                sqlBuilder.append(subject.selectItem.stream()
                        .map(x -> EntityUtils.getColumnNameByFieldName(domainClass, x))
                        .collect(Collectors.joining(",")));
            }
            sqlBuilder.append(" ");
        }
        return sqlBuilder.toString();
    }
    
    public String getWhereConditionStr(MapperMethodArgsMap argsMap) {
        if (whereCondition.size() == 0) {
            return " ";
        } else if (whereCondition.size() == 1) {
            return " WHERE " + whereCondition.get(0).getCriteria(argsMap);
        } else {
            return " WHERE " + whereCondition.stream().map(x -> "(" + x.getCriteria(argsMap) + ")")
                    .collect(Collectors.joining(" OR "));
        }
    }
    
    @Override
    public String toString() {
        return getSql(new MapperMethodArgsMap(new HashMap<>(8)));
    }
    
    private static String[] split(String text, String keyword) {
        Pattern pattern = Pattern.compile(String.format(KEYWORD_TEMPLATE, keyword));
        return pattern.split(text);
    }
    
    public class Subject {
        
        private boolean distinct;
        
        private boolean count;
        
        private boolean exists;
        
        private boolean delete;
        
        private UpdateModeEnum updateMode;
        
        private Integer maxResults;
        
        private List<String> selectItem;
        
        public Subject(String subject) {
            this.updateMode = initUpdateMode(subject);
            if (updateMode != null) {
                return;
            }
            this.delete = matches(subject, DELETE_BY_TEMPLATE);
            if (delete) {
                return;
            }
            this.distinct = subject == null ? false : subject.contains(DISTINCT);
            this.count = matches(subject, COUNT_BY_TEMPLATE);
            this.exists = matches(subject, EXISTS_BY_TEMPLATE);
            if (exists) {
                this.maxResults = 1;
            } else {
                this.maxResults = initMaxResults(subject);
            }
        }
        
        private UpdateModeEnum initUpdateMode(String subject) {
            Matcher grp = UPDATE_BY_TEMPLATE.matcher(subject);
            if (!grp.find()) {
                return null;
            }
            argIndex++;
            String mode = grp.group(2);
            if ("Exact".equals(mode)) {
                return UpdateModeEnum.EXACT;
            } else if ("NotNull".equals(mode)) {
                return UpdateModeEnum.NOT_NULL;
            } else if ("All".equals(mode)) {
                return UpdateModeEnum.ALL;
            } else {
                return UpdateModeEnum.DEFAULT;
            }
        }
        
        private Integer initMaxResults(String subject) {
            if (subject == null) {
                return null;
            }
            Matcher grp = LIMITED_QUERY_TEMPLATE.matcher(subject);
            if (!grp.find()) {
                return null;
            }
            return PrivateStringUtils.isNotBlank(grp.group(4)) ? Integer.valueOf(grp.group(4)) : 1;
        }
        
        private boolean matches(String subject, Pattern pattern) {
            return subject == null ? false : pattern.matcher(subject).find();
        }
    }
    
    public class OrPart {
        
        private List<Part> children = new ArrayList<>();
        
        OrPart(String source) {
            String[] split = split(source, "And");
            for (String part : split) {
                if (PrivateStringUtils.isNotBlank(part)) {
                    children.add(new Part(part));
                }
            }
        }
        
        public String getCriteria(MapperMethodArgsMap argsMap) {
            return children.stream().map(x -> x.getCriteria(argsMap)).collect(Collectors.joining(" AND "));
        }
    }
    
    public class Part {
        
        public final String fieldName;
        
        public final Type type;
        
        public final int currentArgIndex;
        
        public Part(String source) {
            this.type = Type.fromProperty(source);
            this.fieldName = type.extractProperty(source);
            currentArgIndex = argIndex;
            argIndex = argIndex + type.numberOfArguments;
        }
        
        public String getCriteria(MapperMethodArgsMap argsMap) {
            String fieldName = EntityUtils.getColumnNameByFieldName(domainClass, this.fieldName);
            String sqlItem = type.getSqlItem();
            String firstParamName = null;
            if (type.numberOfArguments > 0) {
                firstParamName = argsMap.getKeyName(currentArgIndex);
            }
            if (type.propertyAndArgConsumer != null) {
                TwoTuple<String, Object> propertyAndArg = new TwoTuple<>(fieldName, argsMap.get(currentArgIndex));
                type.propertyAndArgConsumer.accept(propertyAndArg);
                fieldName = propertyAndArg.getFirst();
                firstParamName = argsMap.getAdditionalParameters().setParam(propertyAndArg.getSecond());
                
            }
            if (type.numberOfArguments == 2) {
                int argIndex1 = sqlItem.indexOf("?");
                int argIndex2 = sqlItem.lastIndexOf("?");
                sqlItem = sqlItem.substring(0, argIndex1) + firstParamName + sqlItem.substring(argIndex1 + 1, argIndex2)
                        + argsMap.getKeyName(currentArgIndex + 1) + sqlItem.substring(argIndex2 + 1);
            } else if (type.numberOfArguments == 1) {
                int argIndex = sqlItem.indexOf("?");
                if (type == Type.IN || type == Type.NOT_IN) {
                    Collection<Object> inArgsList = (Collection) argsMap.get(currentArgIndex);
                    String values = inArgsList.stream().map(x -> argsMap.getAdditionalParameters().setParam(x))
                            .collect(Collectors.joining(","));
                    sqlItem = sqlItem.substring(0, argIndex) + "(" + values + ")" + sqlItem.substring(argIndex + 1);
                } else {
                    sqlItem = sqlItem.substring(0, argIndex) + firstParamName + sqlItem.substring(argIndex + 1);
                }
            }
            return fieldName + " " + sqlItem;
        }
        
        @Override
        public String toString() {
            return String
                    .format("%s %s", EntityUtils.getColumnNameByFieldName(domainClass, fieldName), type.getSqlItem());
        }
    }
    
    private static String BLOCK_SPLIT = "(?<=Asc|Desc)(?=\\p{Lu})";
    
    private static Pattern DIRECTION_SPLIT = Pattern.compile("(.+?)(Asc|Desc)?$");
    
    private static Set<String> DIRECTION_KEYWORDS = new HashSet<>(Arrays.asList("Asc", "Desc"));
    
    public class OrderBySource {
        
        private List<TwoTuple<String, Direction>> orders;
        
        public OrderBySource(String clause) {
            if (PrivateStringUtils.isEmpty(clause)) {
                return;
            }
            this.orders = new ArrayList<>();
            for (String part : clause.split(BLOCK_SPLIT)) {
                Matcher matcher = DIRECTION_SPLIT.matcher(part);
                if (!matcher.find()) {
                    throw new IllegalArgumentException("Invalid order syntax for part :" + part);
                }
                String propertyString = matcher.group(1);
                String directionString = matcher.group(2);
                if (DIRECTION_KEYWORDS.contains(propertyString) && directionString == null) {
                    throw new IllegalArgumentException("Invalid order syntax for part :" + part);
                }
                Direction direction =
                        PrivateStringUtils.isNotBlank(directionString) ? getDirectionFromString(directionString) : null;
                this.orders.add(new TwoTuple<>(TruckUtils.uncapitalize(propertyString), direction));
            }
        }
        
        @Override
        public String toString() {
            if (TruckUtils.isEmpty(orders)) {
                return "";
            }
            List<String> orderStr = new ArrayList<>();
            for (TwoTuple<String, Direction> order : orders) {
                orderStr.add(EntityUtils.getColumnNameByFieldName(domainClass, order.getFirst()) + " " + (
                        order.getSecond() == null ? "" : order.getSecond().name()));
            }
            return "ORDER BY " + PrivateStringUtils.join(orderStr, ",");
        }
    }
    
    public static Direction getDirectionFromString(String value) {
        try {
            return Direction.valueOf(value.toUpperCase(Locale.US));
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format(
                    "Invalid value '%s' for orders given! Has to be either 'desc' or 'asc' (case insensitive).", value),
                    e);
        }
    }
    
    public enum Direction {
        ASC,
        DESC;
    }
    
    public enum Type {
        
        BETWEEN(" BETWEEN ? AND ?", 2, "IsBetween", "Between"),
        IS_NOT_NULL(" IS NOT NULL ", 0, "IsNotNull", "NotNull"),
        IS_NULL(" IS NULL ", 0, "IsNull", "Null"),
        LESS_THAN(" < ? ", "IsLessThan", "LessThan"),
        LESS_THAN_EQUAL(" <= ? ", "LessThanEqual"),
        GREATER_THAN(" > ? ", "IsGreaterThan", "GreaterThan"),
        GREATER_THAN_EQUAL(" >= ? ", "IsGreaterThanEqual", "GreaterThanEqual"),
        BEFORE(" < ? ", "IsBefore", "Before"),
        AFTER(" > ? ", "IsAfter", "After"),
        NOT_LIKE(" NOT LIKE ? ", "IsNotLike", "NotLike"),
        LIKE(" LIKE ? ", "IsLike", "Like"),
        STARTING_WITH(" LIKE ? ", 1, (two) -> two.setSecond(two.getSecond() + "%"), "IsStartingWith", "StartingWith",
                "StartsWith"),
        ENDING_WITH(" LIKE ? ", 1, (two) -> two.setSecond("%" + two.getSecond()), "IsEndingWith", "EndingWith",
                "EndsWith"),
        NOT_CONTAINING(" NOT LIKE ? ", 1, (two) -> two.setSecond("%" + two.getSecond() + "%"), "IsNotContaining",
                "NotContaining", "NotContains"),
        CONTAINING(" LIKE ? ", 1, (two) -> two.setSecond("%" + two.getSecond() + "%"), "IsContaining", "Containing",
                "Contains"),
        NOT_IN(" NOT IN ? ", "IsNotIn", "NotIn"),
        IN(" IN ? ", "IsIn", "In"),
        /*NEAR("//TODO", "IsNear", "Near"),
        WITHIN("//TODO", "IsWithin", "Within"),*/
        REGEX(" REGEXP ", "MatchesRegex", "Matches", "Regex"),
        EXISTS(" EXSITS ", 0, "Exists"),
        TRUE(" = true ", 0, "IsTrue", "True"),
        FALSE(" = false ", 0, "IsFalse", "False"),
        NEGATING_SIMPLE_PROPERTY("<> ? ", "IsNot", "Not"),
        IGNORE_CASE(" = UPPER(?) ", 1, (two) -> two.setFirst("UPPER(" + two.getFirst() + ")"), "IgnoreCase"),
        SIMPLE_PROPERTY(" = ? ", "Is", "Equals");
        
        private static final List<Type> ALL = Arrays
                .asList(IS_NOT_NULL, IS_NULL, BETWEEN, LESS_THAN, LESS_THAN_EQUAL, GREATER_THAN, GREATER_THAN_EQUAL,
                        BEFORE, AFTER, NOT_LIKE, LIKE, STARTING_WITH, ENDING_WITH, NOT_CONTAINING, CONTAINING, NOT_IN,
                        IN, REGEX, EXISTS, TRUE, FALSE, NEGATING_SIMPLE_PROPERTY, IGNORE_CASE, SIMPLE_PROPERTY);
        
        public static final Collection<String> ALL_KEYWORDS;
        
        static {
            List<String> allKeywords = new ArrayList<>();
            for (Type type : ALL) {
                allKeywords.addAll(type.keywords);
            }
            ALL_KEYWORDS = Collections.unmodifiableList(allKeywords);
        }
        
        private final List<String> keywords;
        
        private final int numberOfArguments;
        
        private final String sqlItem;
        
        private final Consumer<TwoTuple<String, Object>> propertyAndArgConsumer;
        
        
        Type(String sqlItem, int numberOfArguments, String... keywords) {
            this(sqlItem, numberOfArguments, null, keywords);
        }
        
        Type(String sqlItem, int numberOfArguments, Consumer<TwoTuple<String, Object>> propertyAndArgConsumer,
                String... keywords) {
            this.sqlItem = sqlItem;
            this.numberOfArguments = numberOfArguments;
            this.propertyAndArgConsumer = propertyAndArgConsumer;
            this.keywords = Arrays.asList(keywords);
        }
        
        Type(String sqlItem, String... keywords) {
            this(sqlItem, 1, keywords);
        }
        
        public static Type fromProperty(String rawProperty) {
            
            for (Type type : ALL) {
                if (type.supports(rawProperty)) {
                    return type;
                }
            }
            
            return SIMPLE_PROPERTY;
        }
        
        public Collection<String> getKeywords() {
            return Collections.unmodifiableList(keywords);
        }
        
        protected boolean supports(String property) {
            if (keywords == null) {
                return true;
            }
            for (String keyword : keywords) {
                if (property.endsWith(keyword)) {
                    return true;
                }
            }
            return false;
        }
        
        public int getNumberOfArguments() {
            return numberOfArguments;
        }
        
        public String extractProperty(String part) {
            String candidate = TruckUtils.uncapitalize(part);
            for (String keyword : keywords) {
                if (candidate.endsWith(keyword)) {
                    return candidate.substring(0, candidate.length() - keyword.length());
                }
            }
            return candidate;
        }
        
        public String getSqlItem() {
            return sqlItem;
        }
        
        @Override
        public String toString() {
            return String.format("%s (%s): %s", getSqlItem(), getNumberOfArguments(), getKeywords());
        }
    }
}

