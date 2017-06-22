package com.chetuan.askapp.controller;

import io.netty.channel.Channel;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.chetuan.askapp.constant.Global;
import com.chetuan.askapp.dispatch.DispatchListener;
import com.chetuan.askapp.dispatch.DispatchMethod;
import com.chetuan.askapp.model.Code;
import com.chetuan.askapp.model.User;
import com.chetuan.askapp.model.ModelPrt.Request;
import com.chetuan.askapp.model.ModelPrt.Request.Login;
import com.chetuan.askapp.model.ModelPrt.Request.Logout;
import com.chetuan.askapp.model.ModelPrt.Request.Registe;
import com.chetuan.askapp.model.ModelPrt.Request.SendCode;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.LoginRST;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.RegisteRST;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem.SendCodeRST;
import com.chetuan.askapp.redis.dao.CodeDao;
import com.chetuan.askapp.redis.dao.UserDao;
import com.chetuan.askapp.util.ConfigUtil;
import com.chetuan.askapp.util.ResponceUtil;

public class UserController extends BaseController {

	private Map<String, Boolean> driverStatus = new HashMap<String, Boolean>();

	@Override
	protected void addListener() {
		dispatchCenter.addDispatchListener(UserController.class.getSimpleName() + ".clearDriverStatus", new DispatchListener() {
			@Override
			public void onChannelRemove(String deviceId, Channel channel) {
				String driverId = dispatchCenter.getBindUser(deviceId);
				if (driverId != null) {
					driverStatus.remove(driverId);
				}
			}
		});
	}

	public boolean isDriverAvailabel(String driverId) {
		Boolean status = driverStatus.get(driverId);
		return status == null || status;
	}

	public void updateDriverStatus(String driverId, boolean status) {
		if (status == true) {
			driverStatus.remove(driverId);
		} else {
			driverStatus.put(driverId, false);
		}
	}

	public boolean isUserLogin(String userId, String tempToken) {
		if (userId == null) {
			return false;
		}
		String deviceId = dispatchCenter.getBindDevice(userId);
		if (deviceId != null) {
			return ("" + deviceId.hashCode()).equals(tempToken);
		}
		return false;
	}

	@DispatchMethod(path = "user/sendCode")
	public void sendCode(Request request) {
		SendCode sendCode = request.getSendCode();
		if (sendCode != null) {
			ResponceItem responceItem = null;

			String phone = sendCode.getPhone();
			User user = UserDao.getUserMapByPhone(phone);
			if (user != null) {
				responceItem = ResponceUtil.createResponceItem(request.getId(), false, "该手机号已被注册","0001");
			} else {
				Code code = CodeDao.get(phone);
				long now = new Date().getTime();
				if (code != null && now - code.time <= ConfigUtil.codeSendInterval()) {
					// 距离上一次验证码发送的时间还没有到60秒，无法再次发送验证码
					SendCodeRST sendCodeRST = SendCodeRST.newBuilder().setNextRemain(60000 - now + code.time).build();
					responceItem = ResponceUtil.createResponceItem(request.getId(), false, "距离上一次验证码发送的时间还没有到60秒，无法再次发送验证码","0002", sendCodeRST);
				} else {
					int randCode = 666666;// (int) (Math.random() * 998999 +1000);
					System.out.println("randCode = " + randCode);

					code = new Code();
					code.code = "" + randCode;
					code.phone = phone;
					code.time = new Date().getTime();
					CodeDao.save(code);
					// TODO 发送验证码

					SendCodeRST sendCodeRST = SendCodeRST.newBuilder().setNextRemain(60000).build();
					responceItem = ResponceUtil.createResponceItem(request.getId(), true, "验证码发送成功","0000",sendCodeRST);
				}
			}

			if (responceItem != null) {
				dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
			}
		}
	}

	@DispatchMethod(path = "user/registe")
	public void registe(Request request) {
		Registe registe = request.getRegiste();
		if (registe != null) {
			ResponceItem responceItem = null;

			long now = new Date().getTime();
			Code code = CodeDao.get(registe.getPhone());
			User user = UserDao.getUserMapByPhone(registe.getPhone());

			if (user != null) {
				responceItem = ResponceUtil.createResponceItem(request.getId(), false, "该手机号已被注册","0001");
			} else if (code != null && code.code.equals(registe.getCode())) {
				if (now - code.time > ConfigUtil.codeExpire() * 1000) {
					// 验证码过期
					responceItem = ResponceUtil.createResponceItem(request.getId(), false, "验证码已过期，请重新获取","0003");
				} else {
					boolean flag = false;
					user = new User();
					user.phone = registe.getPhone();
					user.password = registe.getPassword();
					user.token = "";
					user.registe_time = new Date().getTime();

					flag = UserDao.register(user);

					if (flag) {
						RegisteRST registeRST = RegisteRST.newBuilder().setId(user.id).setPhone(user.phone).build();
						responceItem = ResponceUtil.createResponceItem(request.getId(), true, "注册成功", "0000",registeRST);
					} else {
						responceItem = ResponceUtil.createResponceItem(request.getId(), false, "注册失败，请重试","1111");
					}
				}
			} else {
				responceItem = ResponceUtil.createResponceItem(request.getId(), false, "验证码错误","0004");
			}

			if (responceItem != null) {
				dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
			}
		}
	}

	@DispatchMethod(path = "user/login")
	public void login(Request request) {
		Login login = request.getLogin();
		if (login != null) {
			int loginSuccessType = -1;
			ResponceItem responceItem = null;

			long now = new Date().getTime();
			String pwd = login.getPassword();
			User user = UserDao.getUserMapByPhone(login.getPhone());
			if (user == null) {
				responceItem = ResponceUtil.createResponceItem(request.getId(), false, "该手机号还没有注册","0008");
			} else if (StringUtils.isNotEmpty(login.getToken())) {
				// 使用token登录
				if (login.getToken().equals(user.token) && user.token_expire_time >= now) {
					// token相同，且没有过期，登陆成功
					loginSuccessType = 0;
				} else {
					responceItem = ResponceUtil.createResponceItem(request.getId(), false, "token错误或已过期，请重新登陆","0007");
				}
			} else if (pwd != null && user.password.equals(pwd)) {
				
					// 登陆成功，更新token和token过期时间
					loginSuccessType = 1;
					
			} else {
				
				responceItem = ResponceUtil.createResponceItem(request.getId(), false, "密码错误","0006");
			}

			if (loginSuccessType != -1) {
				// 登陆成功
				if (loginSuccessType == 1) {
					user.token = UUID.randomUUID().toString();
					user.token_expire_time = now + ConfigUtil.tokenExpire();
					UserDao.updateUser(user);
				}

				String userId = "" + user.id;
				String oldDeviceId = dispatchCenter.getBindDevice(userId);
				if (oldDeviceId != null && !oldDeviceId.equals(request.getDeviceId())) {
					// 用户在其他的客户端已经登录，需要将其强制下线，不关闭通道，而是用户在之前的客户端上的登录状态消失,
					// 并且将Channel重新绑定到deviceId上, 接触用户和旧设备的绑定关系
					dispatchCenter.unbindUserDevice(userId);
					Channel oldChanel = dispatchCenter.getChannelByDeviceId(oldDeviceId);
					if (oldChanel != null) {
						dispatchCenter.writeToChannel(ResponceUtil.createResponceItem(Global.PUSH_FORCE_LOGOUT, true, "用户在其他客户端登录，当前客户端强制下线","0005"), oldChanel);
					}
				}

				// 登录成功，添加用户_设备绑定关系
				dispatchCenter.bindUserDevice(userId, request.getDeviceId());

				LoginRST loginRST = LoginRST.newBuilder().setId(user.id).setName(user.name).setPhone(user.phone).setToken(user.token)
						.setTempToken("" + request.getDeviceId().hashCode()).build();
				responceItem = ResponceUtil.createResponceItem(request.getId(), true, "登陆成功","0000", loginRST);
			}

			if (responceItem != null) {
				dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
			}
		}
	}

	@DispatchMethod(path = "user/logout")
	public void logout(Request request) {
		Logout logout = request.getLogout();
		if (logout != null) {
			ResponceItem responceItem = null;

			String userId = logout.getUserid();
			User user = UserDao.getUserMapById(userId);
			if (user != null && logout.getToken().equals(user.token)) {
				user.token = "";
				user.token_expire_time = 0;
				if (UserDao.updateUser(user)) {
					responceItem = ResponceUtil.createResponceItem(request.getId(), true, "退出登录成功","0000");
					dispatchCenter.unbindUserDevice(userId);
				} else {
					responceItem = ResponceUtil.createResponceItem(request.getId(), false, "退出登录失败","0000");
				}
			} else {
				responceItem = ResponceUtil.createResponceItem(request.getId(), false, "退出登录失败","0000");
			}

			if (responceItem != null) {
				dispatchCenter.writeToChannel(responceItem, dispatchCenter.getChannelByDeviceId(request.getDeviceId()));
			}
		}
	}

}
