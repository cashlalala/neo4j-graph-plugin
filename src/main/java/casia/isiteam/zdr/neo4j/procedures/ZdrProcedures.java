package casia.isiteam.zdr.neo4j.procedures;
/**
 * 　　　　　　　 ┏┓       ┏┓+ +
 * 　　　　　　　┏┛┻━━━━━━━┛┻┓ + +
 * 　　　　　　　┃　　　　　　 ┃
 * 　　　　　　　┃　　　━　　　┃ ++ + + +
 * 　　　　　　 █████━█████  ┃+
 * 　　　　　　　┃　　　　　　 ┃ +
 * 　　　　　　　┃　　　┻　　　┃
 * 　　　　　　　┃　　　　　　 ┃ + +
 * 　　　　　　　┗━━┓　　　 ┏━┛
 * ┃　　  ┃
 * 　　　　　　　　　┃　　  ┃ + + + +
 * 　　　　　　　　　┃　　　┃　Code is far away from     bug with the animal protecting
 * 　　　　　　　　　┃　　　┃ +
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃　　+
 * 　　　　　　　　　┃　 　 ┗━━━┓ + +
 * 　　　　　　　　　┃ 　　　　　┣┓
 * 　　　　　　　　　┃ 　　　　　┏┛
 * 　　　　　　　　　┗┓┓┏━━━┳┓┏┛ + + + +
 * 　　　　　　　　　 ┃┫┫　 ┃┫┫
 * 　　　　　　　　　 ┗┻┛　 ┗┻┛+ + + +
 */

import casia.isiteam.zdr.neo4j.result.NodeResult;
import casia.isiteam.zdr.neo4j.util.ChineseVerify;
import casia.isiteam.zdr.neo4j.util.DateHandle;
import casia.isiteam.zdr.neo4j.util.NodeHandle;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YanchaoMa yanchaoma@foxmail.com
 * @PACKAGE_NAME: casia.isiteam.zdr.neo4j.procedures
 * @Description: TODO(执行查询的存储过程 / 函数)
 * @date 2018/8/3 17:54
 */
public class ZdrProcedures {

    /**
     * @param world:函数参数
     * @return
     * @Description: TODO(@ Description的内容会在Neo4j浏览器中调用dbms.functions () 时显示)
     */
    @UserFunction(name = "zdr.apoc.hello") // 自定义函数名
    @Description("hello(world) - Say hello!")   // 函数说明
    public String hello(@Name("world") String world) {
        return String.format("Hello, %s", world);
    }

    /**
     * @param
     * @return
     * @Description: TODO(自定义函数 - 降序排序集合的元素)
     */
    @UserFunction(name = "zdr.apoc.sortDESC")
    public List<Object> sortDESC(@Name("coll") List<Object> coll) {
        List sorted = new ArrayList<>(coll);
        Collections.sort(sorted, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Integer o1Int = Integer.valueOf(String.valueOf(o1));
                Integer o2Int = Integer.valueOf(String.valueOf(o2));
                return o2Int.compareTo(o1Int);
            }
        });
        return sorted;
    }

    /**
     * @param
     * @return
     * @Description: TODO(获取命中关键词关系的属性ids的长度 ， ids的值是用逗号分隔的id)
     */
    @UserFunction(name = "zdr.apoc.getEventIdsSize")
    public Number getEventIdsSize(@Name("ids") String ids) {
        String[] array = ids.split(",");
        int eventIdsSize = array.length;
        return eventIdsSize;
    }

    /**
     * @param
     * @return
     * @Description: TODO(截取时间的年份)
     */
    @UserFunction(name = "zdr.apoc.initAnnualTime")
    public long convertInitAnnualTime(@Name("startTime") String startTime) {
        if (startTime != null) {
            String[] array = startTime.split("-");
            if (array.length == 3) {
                return Long.valueOf(array[0]);
            } else {
                return 0;
            }
        }
        return 0;
    }

    /**
     * @param
     * @return 1、符合时间区间 0、不符合时间区间
     * @Description: TODO(判断通联时间段是否匹配)
     */
    @UserFunction(name = "zdr.apoc.matchTimeZone")
    public Number matchTimeZone(@Name("mapPara") Map<String, String> mapPara) {

        String startTime = mapPara.get("startTime");
        String stopTime = mapPara.get("stopTime");

        DateHandle dateHandle = new DateHandle();
        long startTimeLong = dateHandle.dateToMillisecond(startTime);
        long stopTimeLong = dateHandle.dateToMillisecond(stopTime);

        String timeListString = mapPara.get("timeList");
        String[] timeArray = timeListString.split(",");
        String time;
        long timeLong;
        int size = timeArray.length;
        for (int i = 0; i < size; i++) {
            time = timeArray[i];
            timeLong = dateHandle.dateToMillisecond(time);
            if (startTimeLong <= timeLong && timeLong <= stopTimeLong) {
                return 1;   // 符合时间区间返回1
            }
        }
        return 0;   // 不符合时间区间返回0
    }

    /**
     * @param
     * @return
     * @Description: TODO(找出匹配的时间段)
     */
    @UserFunction(name = "zdr.apoc.matchTimeListString")
    public String matchTimeListString(@Name("mapPara") Map<String, String> mapPara) {
        String startTime = mapPara.get("startTime");
        String stopTime = mapPara.get("stopTime");

        DateHandle dateHandle = new DateHandle();
        long startTimeLong = dateHandle.dateToMillisecond(startTime);
        long stopTimeLong = dateHandle.dateToMillisecond(stopTime);

        String timeListString = mapPara.get("timeList");
        String[] timeArray = timeListString.split(",");
        StringBuilder builder = new StringBuilder();
        String time;
        long timeLong;
        int size = timeArray.length;
        for (int i = 0; i < size; i++) {
            time = timeArray[i];
            timeLong = dateHandle.dateToMillisecond(time);
            if (startTimeLong <= timeLong && timeLong <= stopTimeLong) {
                builder.append(time + ",");
            }
        }
        String timeList = null;
        if (builder != null && !"".equals(builder.toString())) {
            timeList = builder.substring(0, builder.toString().length() - 1);
        }
        return timeList;
    }

    /**
     * @param
     * @return
     * @Description: TODO(百分比映射)
     */
    @UserFunction(name = "zdr.apoc.scorePercentage")
    @Description("Set node influence score percentage")
    public Number percentageInfluenceScore(@Name("mapPara") Map<String, Object> mapPara) {

        double max = shiftDouble(mapPara.get("maxScore"));
        double min = shiftDouble(mapPara.get("minScore"));
        double current = shiftDouble(mapPara.get("currentScore"));

        // min-max标准化(Min-MaxNormalization)也称为离差标准化，是对原始数据的线性变换，使结果值映射到 [0 - 1] 之间
        double initialThreshold = 0.015;
        if (min <= current && current <= max && min != 0) {
            double percentage = (current - min) / (max - min);
            double percentageFormat = Double.parseDouble(String.format("%.6f", percentage));
            if (percentageFormat == 0) {
                return initialThreshold;
            }
            return percentageFormat;
        } else {
            return initialThreshold;
        }
    }

    /**
     * @param
     * @return
     * @Description: TODO(统一数据类型)
     */
    private double shiftDouble(Object dataObject) {
        if (dataObject instanceof Long) {
            Long data = (Long) dataObject;
            return data.doubleValue();

        } else if (dataObject instanceof Double) {
            return (double) dataObject;

        } else if (dataObject instanceof Integer) {
            Integer data = (Integer) dataObject;
            return data.doubleValue();

        } else if (dataObject instanceof Float) {
            Float data = (Float) dataObject;
            return data.doubleValue();
        } else {
            return 0;
        }
    }

    /**
     * @param
     * @return
     * @Description: TODO(小数点向后移动)
     */
    @UserFunction(name = "zdr.apoc.moveDecimalPoint")
    @Description("Move six decimal points")
    public Number moveDecimalPoint(@Name("mapPara") Map<String, Object> mapPara) {
        double scoreObject = shiftDouble(mapPara.get("scoreObject"));
        double moveLength = shiftDouble(mapPara.get("moveLength"));
        BigDecimal score = BigDecimal.valueOf(scoreObject);
        score = score.multiply(BigDecimal.valueOf(moveLength));
        BigInteger scoreInt = score.toBigInteger();
        return scoreInt.intValue();
    }

    /**
     * @param
     * @return
     * @Description: TODO(Present字符转换获取当前系统时间)
     */
    @UserFunction(name = "zdr.apoc.presentStringToDate")
    @Description("Present-Convert date to relevant format")
    public String presentStringToDate(@Name("present") String present) {
        if ("Present".equals(present)) {
            DateHandle dateHandle = new DateHandle();
            return dateHandle.millisecondToDate(System.currentTimeMillis());
        }
        return present;
    }

    /**
     * @param
     * @return
     * @Description: TODO(判断两个时间区间是否有交叉)
     */
    @UserFunction(name = "zdr.apoc.timeCrossOrNot")
    @Description("Time zone cross or not")
    public boolean timeCrossOrNot(@Name("mapPara") Map<String, Object> mapPara) {

        DateHandle dateHandle = new DateHandle();
        String r1Start = (String) mapPara.get("r1Start");
        String r1Stop = (String) mapPara.get("r1Stop");
        String r2Start = (String) mapPara.get("r2Start");
        String r2Stop = (String) mapPara.get("r2Stop");

        if ((dateHandle.objectToDate(r1Start) || dateHandle.objectToDate(r1Stop)) && (dateHandle.objectToDate(r2Start)
                || dateHandle.objectToDate(r2Stop))) {

            long r1StartMill = dateHandle.dateToMillisecond(r1Start);
            long r1StopMill = dateHandle.dateToMillisecond(r1Stop);
            long r2StartMill = dateHandle.dateToMillisecond(r2Start);
            long r2StopMill = dateHandle.dateToMillisecond(r2Stop);

            // 不可能交叉的情况：
            // 1、区间一的结束时间小于区间二的开始时间
            // 2、区间一的开始时间大于区间二的结束时间

            if ((r1StartMill > r2StopMill && r1StartMill != 0 && r2StopMill != 0)
                    || (r1StopMill < r2StartMill && r1StopMill != 0 && r2StartMill != 0)
                    || (r1StartMill > r2StartMill && r2StopMill == 0)
                    || (r1StartMill < r2StartMill && r1StopMill == 0)) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * @param
     * @return
     * @Description: TODO(是否包含某字符串 | | - 任意包含一个 & & - 全部包含)
     */
    @UserFunction(name = "zdr.apoc.isContainsString")
    @Description("Is contains string? &&-All contains ||-Or contains (Chinese||English Chinese&&English)")
    public boolean isContainsString(@Name("mapPara") Map<String, Object> mapPara) {

        // 将输入拼接成一个STRING
        String original = removeNull(mapPara.get("original0")) + removeNull(mapPara.get("original1")) + removeNull(mapPara.get("original2")) +
                removeNull(mapPara.get("original3")) + removeNull(mapPara.get("original4")) + removeNull(mapPara.get("original5")) + removeNull(mapPara.get("original6")) +
                removeNull(mapPara.get("original7")) + removeNull(mapPara.get("original8")) + removeNull(mapPara.get("original9"));
        String input = (String) mapPara.get("input");

        if (original != null && !"".equals(original)) {
            String[] split;
            if (input.contains("||")) {
                split = input.split("\\|\\|");
                return Arrays.stream(split).parallel().anyMatch(v -> original.contains(v));
            } else if (input.contains("&&")) {
                split = input.split("&&");
                return Arrays.stream(split).parallel().allMatch(v -> original.contains(v));
            } else if (original.contains(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param
     * @return
     * @Description: TODO(清理NULL值设置为空字符串)
     */
    private String removeNull(Object object) {
        if (object == null) {
            return "";
        }
        return String.valueOf(object);
    }

    /**
     * @param
     * @return
     * @Description: TODO(统计字符串中包含某个字符的数量)
     */
    @UserFunction(name = "zdr.apoc.stringCharCount")
    @Description("Count char in string")
    public long stringCharCount(@Name("mapPara") Map<String, Object> mapPara) {
        return StringUtils.countMatches((String) mapPara.get("original"), (String) mapPara.get("char"));
    }

    /**
     * @param nLabels:节点集合
     * @param mLabels:节点集合
     * @param strictLabels:标签 分隔符号（||）
     * @return 两个集合同时包含某一个标签 返回TRUE
     * @Description: TODO(两个集合同时包含某一个标签)
     */
    @UserFunction(name = "zdr.apoc.relatCalculateRestrict")
    @Description("Graph relationships calculate restrict")
    public boolean relatCalculateRestrict(@Name("nLabels") List<String> nLabels, @Name("mLabels") List<String> mLabels, @Name("restrictLabels") String strictLabels) {

        // ||包含其中一个
        if (strictLabels.contains("||")) {
            String[] strict = strictLabels.split("\\|\\|");
            for (int i = 0; i < strict.length; i++) {
                String label = strict[i];
                if (nLabels.contains(label) && mLabels.contains(label)) {
                    return true;
                }
            }
        } else {
            if (nLabels.contains(strictLabels) && mLabels.contains(strictLabels)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断字符串中是否包含中文
     *
     * @param node:节点的所有属性中是否包含中文
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    @UserFunction(name = "zdr.apoc.isContainChinese")
    @Description("Node is contains chinese or not")
    public long isContainChinese(@Name("node") Node node) {

        Iterable<String> iterableKeys = node.getPropertyKeys();
        StringBuilder nodeValueBuilder = new StringBuilder();
        for (Iterator iterator = iterableKeys.iterator(); iterator.hasNext(); ) {
            Object next = iterator.next();
            Object nodeValue = node.getProperty((String) next);
            if (nodeValue instanceof String) {
                nodeValueBuilder.append(nodeValue);
            }
        }
        // 所有节点属性value
        char[] nodeValueChar = nodeValueBuilder.toString().toCharArray();

        int chineseCharCount = 0;
        ChineseVerify chineseVerify = new ChineseVerify();
        for (int i = 0; i < nodeValueChar.length; i++) {
            char c = nodeValueChar[i];
            if (chineseVerify.isContainChinese(String.valueOf(c))) {
                chineseCharCount++;
            }
        }
        return chineseCharCount;
    }

    /**
     * @param
     * @return
     * @Description: TODO(节点是否包含权限)
     */
    @UserFunction(name = "zdr.apoc.isContainAuthority")
    @Description("Node is contains authority or not")
    public boolean isContainAuthority(@Name("node") Node node) {
        Iterable<String> iterableKeys = node.getPropertyKeys();

        String prefix = "sysuser_id_";
        for (Iterator iterator = iterableKeys.iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            if (key.contains(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param
     * @return
     * @Description: TODO(去重合并多个节点列表)
     */
    @UserFunction(name = "zdr.apoc.mergeNodes")
    @Description("Merge node list")
    public List<Node> mergeNodes(@Name("nodePackArray") List<List<Node>> nodePackArray) {

        List<Node> nodes = new ArrayList<>();

        for (int i = 0; i < nodePackArray.size(); i++) {
            List<Node> nodeList = nodePackArray.get(i);
            nodes.addAll(nodeList);
        }
        NodeHandle nodeHandle = new NodeHandle();

        return nodeHandle.distinctNodes(nodes);
    }

    /**
     * @param
     * @return
     * @Description: TODO(地理位置名称多字段检索 -)
     */
    @UserFunction(name = "zdr.apoc.locMultiFieldsFullTextSearchCondition")
    @Description("Location multi fields search- 找共同居住地的人 - EXAMPLE:location:`\"+location+\"`* OR location:`\"+location+\"`*")
    public String locMultiFieldsFullTextSearchCondition(@Name("node") Node node, @Name("locMultiFields") List<String> locMultiFields) {

        StringBuilder builder = new StringBuilder();
        Map<String, Object> mapProperties = node.getAllProperties();
        locMultiFields.forEach(field -> {
            if (!"".equals(field) && field != null) {
                if (mapProperties.containsKey(field)) {
                    Object value = node.getProperty(field);
                    if (value instanceof String) {
                        if (value != null && !"".equals(value)) {
                            builder.append(field + ":`" + value + "`* OR ");
                        }
                    }
                }
            }
        });
        if (builder != null && !"".equals(builder.toString())) {
            return builder.substring(0, builder.length() - 3);
        }
        return "`null`";
    }

    //    zdr.apoc.nodeIsContainsKey

    /**
     * @param
     * @return
     * @Description: TODO(节点是否包含某个KEY - 多个中的任意一个)
     */
    @UserFunction(name = "zdr.apoc.nodeIsContainsKey")
    @Description("Node is contain key or not")
    public boolean nodeIsContainsKey(@Name("node") Node node, @Name("locMultiFields") List<String> locMultiFields) {
        Map<String, Object> mapProperties = node.getAllProperties();
        for (int i = 0; i < locMultiFields.size(); i++) {
            String field = locMultiFields.get(i);
            if (mapProperties.containsKey(field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param
     * @return
     * @Description: TODO(节点转换)
     */
    private List<NodeResult> transformNodes(List<Node> nodes) {
        return nodes.stream().map(node -> new NodeResult(node))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @param
     * @return
     * @Description: TODO(节点ID集合移除某些节点)
     */
    @UserFunction(name = "zdr.apoc.removeIdsFromRawList")
    @Description("Remove ids from raw node id list")
    public List<Long> removeIdsFromRawList(@Name("rawIDs") List<Long> rawIDs, @Name("ids") List<Long> ids) {
        if (rawIDs != null && ids != null) {
            rawIDs.removeAll(ids);
            return rawIDs;
        }
        return rawIDs;
    }

}



