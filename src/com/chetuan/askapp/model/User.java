package com.chetuan.askapp.model;

public class User {

	public long id;// 用户id

	public String name = "";// 用户姓名

	public String phone;// 用户手机号

	public String token = "";// 用户登录凭据

	public long token_expire_time;// token过期时刻，到期后token失效，需要用户重新登录

	public long registe_time;// 用户注册时间

	public String userImage = "";// 头像
	public String nickname = "";

	public String desc = "";// 描述
	public String password = "";

	public String brith = "";// 出生年月
	public String address = "";// 地址
	public String sex = "";// 性别
	public String company = "";// 公司
	public String weichat = "";// 微信
	public String qq = "";
	public String email = "";

}
