package com.chetuan.askapp.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chetuan.askapp.model.ModelPrt.Responce;
import com.chetuan.askapp.model.ModelPrt.Responce.ResponceItem;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.Type;

public class ResponceUtil {

	private static Map<String, FieldDescriptor> descriptors = new HashMap<String, FieldDescriptor>();
	static {
		Descriptor desc = ResponceItem.getDescriptor();
		List<FieldDescriptor> fileDescriptors = desc.getFields();
		for(FieldDescriptor descriptor: fileDescriptors)
		{
			if(descriptor.getType() == Type.MESSAGE)
			{
				String type = descriptor.getMessageType().getName();
				descriptors.put(type, descriptor);
			}
		}
	}
	
	public static Responce createResponce(ResponceItem responceItem)
	{
		Responce.Builder builder = Responce.newBuilder();
		builder.addResponces(responceItem);
		return builder.build();
	}
	
	public static Responce createResponce(List<ResponceItem> responceItems)
	{
		Responce.Builder builder = Responce.newBuilder();
		builder.addAllResponces(responceItems);
		return builder.build();
	}
	
	public static ResponceItem createResponceItem(String id, boolean success, String msg,Object...datas)
	{
		try {
			ResponceItem.Builder builder = ResponceItem.newBuilder();
			builder.setId(id);
			builder.setSuccess(success);
			builder.setMsg(msg);
			//builder.setCode(code);
			if(datas != null)
			{
				int size = datas.length;
				for(int i = 0; i < size; i ++)
				{
					Object data = datas[i];
					if(data instanceof List)
					{
						List dataList = (List) data;
						int s = dataList.size();
						if(s > 0)
						{
							FieldDescriptor descriptor = descriptors.get(dataList.get(0).getClass().getSimpleName());
							if(descriptor != null)
							{
								for(int j = 0; j < s; j ++)
								{
									builder.addRepeatedField(descriptor, dataList.get(j));
								}
							}
						}
					}
					else {
						FieldDescriptor descriptor = descriptors.get(data.getClass().getSimpleName());
						if(descriptor != null)
						{
							
							builder.setField(descriptor, data);
						}
					}
				}
			}
			return builder.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
