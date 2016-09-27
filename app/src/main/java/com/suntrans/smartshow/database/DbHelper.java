package com.suntrans.smartshow.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	private final static String DATABASE_NAME = "BOOKS.db";
	private final static int DATABASE_VERSION = 1;
	private final static String TABLE_NAME = "books_table";
	public final static String BOOK_ID = "book_id";
	public final static String BOOK_NAME = "book_name";
	public final static String BOOK_AUTHOR = "book_author";
	public DbHelper(Context context, String name, CursorFactory factory,
					int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub      INTEGER primary key autoincrement自增长
		//String sql = "drop table if exists switch_tb " ;
		String sql = " create table  switchs_tb ( CID INTEGER primary key autoincrement,"  //通道ID 唯一标示通道 ，自增长
				+ "State TEXT,"   //开关状态   "开"或“关”
				+ "Room TEXT,"   //开关所属房间
				+ "Editable TEXT,"   //本通道是否可以控制，1代表可以控制，0代表不可以进行控制（只显示状态）
				+ "MainAddr TEXT,"  //开关所属的第六感官的地址，4个长度的字符串，如0001，0002等
				+ "RSAddr TEXT,"     //开关的485地址，8个长度的字符串，如00000001,00000002等
				+ "Channel TEXT,"  //某个开关的Channel号  从0到10   0代表总开关
				+ "Area TEXT,"     //该通道所在的区域，厨房、卧室、客厅等
				+ "Name TEXT,"     //通道的别名，如客厅灯，插座。。等
				+ "VoiceName TEXT,"  //语音的名称，用于保存配置的开关通道的语音指令
				+ "IsShow TEXT,"    //当前图标是否显示，为1则代表显示，为0则代表不显示
				+ "Matrix TEXT,"   //该通道图片的参数矩阵matrix中的9个参数值，每个值后面都有分号（";"）隔开
				+ "Image BLOB);";    //开关图标
		db.execSQL(sql);     //创建开关表

		String sql2 = " create table  users_tb ( NID INTEGER primary key autoincrement,"  //用户名ID 唯一标示通道 ，自增长
				+ "Name TEXT,"   //用户姓名
				+ "Password TEXT,"//用户密码 
				+ "RSAddr TEXT,"     //开关的485地址，4个长度的字符串，如0000,0001
				+ "IsUsing TEXT,"//是否正在使用,1表示正在使用，0表示没有在使用 
				+ "Auto TEXT,"   //是否自动登录，1表示自动登录，0表示不自动登录 
				+ "Remember TEXT);";  //是否记住密码，1表示记住密码，0表示不记住密码
		db.execSQL(sql2);    //创建用户表

		String sql3 = "create table room_tb (RID INTEGER primary key autoincrement,"    //创建房间表，主要保存房间名和房间背景图
				+ "Name TEXT,"  //房间名称
				+ "RSAddr TEXT,"   //房间内第六感官的485地址，一个房间对应一个485地址,例如0001,0002等
				+ "Image BLOB);";    //房间背景图片
		db.execSQL(sql3);   //创建房间表

		String sql4 = " create table  voice_tb ( VID INTEGER primary key autoincrement,"  //音频ID 唯一自增长
				+ "Name TEXT,"     //音频名称
				+ "Pinyin TEXT,"    //拼音
				+ "VoiceNum TEXT);"; //音频对应的地址
		db.execSQL(sql4);    //创建音频表

		String sql5=" create table sixsensor_tb ( SID INTEGER primary key autoincrement,"  //第六感ID 自增长
				+ "Name TEXT,"     //第六感名称
				+ "RSAddr TEXT);";    //第六感地址,xxxx
		db.execSQL(sql5);    //创建第六感表

		String sql6=" create table meter_tb ( MID INTEGER primary key autoincrement,"  //电表ID 唯一自增长
				+ "Name TEXT,"     //电表名称
				+ "RSAddr TEXT,"    //电表地址
				+ "RSAddrOpposite TEXT,"    //反向的电表地址，即协议中用到的地址
				+ "Type TEXT);" ;   //电表类型，1代表单相电表，3代表三相电表
		db.execSQL(sql6);    //创建电表表


	}

	@Override    //数据库版本更新的时候调用
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	//查询操作
	public Cursor select() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query("switch_tb", null, null, null, null, null, null);
		return cursor;
	}
	//增加操作
	public long insert(String bookname, String author)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put(BOOK_NAME, bookname);
		cv.put(BOOK_AUTHOR, author);
		long row = db.insert(TABLE_NAME, null, cv);


		return row;
	}
	//删除操作
	public void delete(int id)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		String where = BOOK_ID + " = ?";
		String[] whereValue ={ Integer.toString(id) };
		db.delete(TABLE_NAME, where, whereValue);
	}
	//修改操作
	public void update(int id, String bookname, String author)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		String where = BOOK_ID + " = ?";
		String[] whereValue = { Integer.toString(id) };

		ContentValues cv = new ContentValues();
		cv.put(BOOK_NAME, bookname);
		cv.put(BOOK_AUTHOR, author);
		db.update(TABLE_NAME, cv, where, whereValue);
	}
}
