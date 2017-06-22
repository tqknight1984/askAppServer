package com.chetuan.askapp.model;

//问题
public class Question {

	public String id ;
	public String questionconetnt="";
	public String addtime="";
	public String userid="";
	public String status="";
	public String userImage="";
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getQuestionconetnt() {
		return questionconetnt;
	}
	public void setQuestionconetnt(String questionconetnt) {
		this.questionconetnt = questionconetnt;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getAddtime() {
		return addtime;
	}
	public void setAddtime(String addtime) {
		this.addtime = addtime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	

}
