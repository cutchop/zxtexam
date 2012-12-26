package net.whzxt.zxtexam;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBer extends SQLiteOpenHelper {
	public static final String T_ROUTE = "zxt_route";// 路线
	public static final String T_ROUTE_ITEM = "zxt_route_item";// 路线项目
	public static final String T_ITEM = "zxt_item";// 项目
	public static final String T_ITEM_ERR = "zxt_item_err";// 项目扣分项
	public static final String T_ITEM_ACTION = "zxt_item_action";// 项目动作

	public DBer(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + T_ROUTE + " (routeid INTEGER PRIMARY KEY,name VARCHAR(20),tts VARCHAR(100))");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + T_ROUTE_ITEM + " (routeid INTEGER,itemid INTEGER,lon FLOAT,lat FLOAT)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + T_ITEM + " (itemid INTEGER PRIMARY KEY,name VARCHAR(20),tts VARCHAR(100),timeout INTEGER)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + T_ITEM_ERR + " (errid INTEGER PRIMARY KEY,itemid INTEGER,name VARCHAR(100),fenshu INTEGER)");
		db.execSQL("CREATE TABLE IF NOT EXISTS " + T_ITEM_ACTION + " (itemid INTEGER,dataid INTEGER,times INTEGER,min INTEGER,max INTEGER,errid INTEGER)");
		// 项目
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (0, '上车准备', '上车准备', 20)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (1, '起步', '起步', 20)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (2, '直线行驶', '直线行驶', 20)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (3, '变更车道', '变更车道', 20)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (4, '通过路口', '通过路口', 20)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (5, '通过人行横道线、学校区域和公共汽车站', '通过人行横道线、学校区域和公共汽车站', 20)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (6, '会车', '会车', 20)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (7, '超车', '超车', 20)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (8, '靠边停车', '靠边停车', 20)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (9, '掉头', '掉头', 20)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (10, '夜间行驶', '夜间行驶', 20)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (11, '夜间在没有路灯、照明不良条件下行驶', '夜间在没有路灯、照明不良条件下行驶', 5)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (12, '请将前大灯换成远光' ,'请将前大灯换成远光', 5)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (13, '夜间同向近距离跟车行驶','夜间同向近距离跟车行驶', 5)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (14, '夜间通过坡路、拱桥','夜间通过坡路、拱桥', 5)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (15, '夜间在道路上发生故障，妨碍交通又难以移动','夜间在道路上发生故障，妨碍交通又难以移动', 5)");
		db.execSQL("INSERT INTO " + T_ITEM + " (itemid, name, tts, timeout) VALUES (16, '模拟夜间考试完成，请关闭所有灯光','模拟夜间考试完成，请关闭所有灯光', 5)");
		// 扣分项
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (0,0, '不绕车一周检查车辆外观及安全状况', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (1,0, '打开车门前不观察后方交通情况', 100)");
		
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (2,1, '制动气压不足起步', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (3,1, '车门未关闭起步', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (4,1, '起步前，未通过后视镜并向左方侧头，观察左、后方交通情况', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (5,1, '启动发动机时，变速器操纵杆未置于空挡(或者P挡)', 10)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (6,1, '发动机启动后，不及时松开启动开关', 10)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (7,1, '不松驻车制动器起步', 10)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (8,1, '道路交通情况复杂时起步不能合理使用喇叭', 10)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (9,1, '起步时车辆发生闯动', 10)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (10,1, '起步时，加速踏板控制不当，致使发动机转速过高', 5)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (11,1, '启动发动机前，不调整驾驶座椅、后视镜、检查仪表', 5)");
		
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (12,2, '方向控制不稳，不能保持车辆直线运动状态', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (13,2, '不了解车辆行驶速度', 10)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (14,2, '未及时发现路面障碍物，未及时采取减速措施', 10)");
		
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (15,3, '变更车道时，判断车辆安全距离不合理，妨碍其他车辆正常行驶', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (16,3, '连续变更两条以上车道', 100)");
		
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (17,4, '通过路口前未减速慢行', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (18,4, '遇有路口交通阻塞时进入路口，将车辆停在路口内等候', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (19,4, '不按规定避让行人和优先通过的车辆', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (20,4, '左转通过路口时，未靠路口中心点左侧转弯', 100)");
		
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (21,5, '不按规定减速慢行', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (22,5, '遇行人通过人行横道不停车让行', 100)");
		
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (23,6, '在没有中心隔离设施或者中心线的道路上会车时，不减速靠右行驶，并与其他车辆、行人或者非机动车未保持安全距离', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (24,6, '横向安全间距判断差，紧急转向避让相对方向来车', 100)");
		
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (25,7, '超车时机选择不合理，影响其他车辆正常行驶', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (26,7, '超车时未与被超越车辆保持安全距离', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (27,7, '超车后急转向驶回本车道，妨碍被超车辆正常行驶', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (28,7, '从右侧超车', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (29,7, '当后车发出超车信号时，具备让车条件不减速靠右让行', 10)");
		
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (30,8, '停车前，不通过内、外后视镜观察后方和右侧交通情况', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (31,8, '停车后，车身超过道路右侧边缘线或者人行道边缘', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (32,8, '停车后，在车内开门前不侧头观察侧后方和左侧交通情况', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (33,8, '停车后，车身距离道路右侧边缘线或者人行道边缘大于30厘米', 20)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (34,8, '停车后，未拉紧驻车制动器', 20)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (35,8, '拉紧驻车制动器前放松行车制动踏板', 10)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (36,8, '下车后不关车门', 10)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (37,8, '下车前不将发动机熄火', 5)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (38,8, '夜间在路边临时停车不关闭前照灯或不开启警示灯', 5)");
		
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (39,9, '不能正确观察交通情况选择掉头时机', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (40,9, '掉头地点选择不当', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (41,9, '掉头时，妨碍正常行驶的其他车辆和行人通行', 100)");
		
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (42,10, '不能正确开启灯光', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (43,10, '同方向近距离跟车行驶时，使用远光灯', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (44,10, '通过急弯、坡路、拱桥、人行横道或者没有交通信号灯控制的路口时，不交替使用远、近光灯示意', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (45,10, '会车时不按规定使用灯光', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (46,10, '在路口转弯时，使用远光灯', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (47,10, '超车时未变换使用远、近光灯提醒被超越车辆', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (48,10, '在有路灯、照明良好的道路上行驶时，使用远光灯', 100)");

		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (49,11, '不能正确开启灯光', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (50,12, '不能正确开启灯光', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (51,13, '不能正确开启灯光', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (52,14, '不能正确开启灯光', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (53,15, '不能正确开启灯光', 100)");
		db.execSQL("INSERT INTO " + T_ITEM_ERR + " (errid, itemid, name, fenshu) VALUES (54,16, '不能正确开启灯光', 100)");
		// 路线
		db.execSQL("INSERT INTO " + T_ROUTE + " (routeid, name, tts) VALUES (0, '随机路线', '当前为随机路线')");
		db.execSQL("INSERT INTO " + T_ROUTE + " (routeid, name, tts) VALUES (1, '灯光', '下面将、进行模拟夜间行驶场景灯光、使用的考试，请按语音指令、在5秒内做出相应的灯光操作')");
		// 路线包含的项目
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (0, 0)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (0, 1)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (0, 2)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (0, 3)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (0, 4)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (0, 5)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (0, 6)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (0, 7)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (0, 8)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (0, 9)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (0, 10)");
		
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (1, 11)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (1, 12)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (1, 13)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (1, 14)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (1, 15)");
		db.execSQL("INSERT INTO " + T_ROUTE_ITEM + " (routeid, itemid) VALUES (1, 16)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + T_ROUTE);
		db.execSQL("DROP TABLE IF EXISTS " + T_ROUTE_ITEM);
		db.execSQL("DROP TABLE IF EXISTS " + T_ITEM);
		db.execSQL("DROP TABLE IF EXISTS " + T_ITEM_ERR);
		db.execSQL("DROP TABLE IF EXISTS " + T_ITEM_ACTION);
		onCreate(db);
	}
}