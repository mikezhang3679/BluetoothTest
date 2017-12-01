# 中文
  1.这个demo里面包含了很多指令，你可以运行这个demo，连接上我们的手环，然后测试手环的功能。
  
  2.如果你想要做自己的APP，你也可以参照我们这个demo，只需要根据蓝牙协议，添加你们自己想要的指令。(蓝牙指令全在CommandManager这个类)。
  
  
# English
  1.This demo contains a lot of instructions, you can run this demo, connect to our bracelet, and then test the function of the bracelet.
  
  2.If you want to make your own APP, you can also refer to our demo, which only needs to add the instructions you want according to the       bluetooth protocol. (The bluetooth instructions are all in the CommandManager class)


		app 连接上手环之后 手环会自动发电量（91）和版本信息（92）过来。
		注意：不要在手环给app发版本信息的时候，向手环发送任何指令，有可能指令会无效。
		
		当你的手环连接上app的时候，手环会自动发送电量和版本信息的数据过来。
		
		电量（ID=91）：
					if (datas.get(0) == 0xAB && datas.get(4) == 0x91) {
                        int batteryPercent = datas.get(7);
                    }
					
					
		版本信息（ID=92） ： 
                    if (datas.get(0) == 0xAB && datas.get(4) == 0x92) { 
                        firmwareVersion = datas.get(6) + (float) datas.get(7) / 100;
                        bandType = datas.get(8);
                    }
		同步时间（ID=93）：
					一旦app连接上手环， 你应该同步手环时间，把你的手机上的正确时间发送到手环上去（注意要等手环发送完电量和版本信息之后再发送这条指令）
					
					
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(System.currentTimeMillis());
						int year = calendar.get(Calendar.YEAR);
						int month = calendar.get(Calendar.MONTH) + 1;
						int day = calendar.get(Calendar.DAY_OF_MONTH);
						int hour = calendar.get(Calendar.HOUR_OF_DAY);
						int minute = calendar.get(Calendar.MINUTE);
						int second = calendar.get(Calendar.SECOND);
						byte[] data = new byte[14];
						data[0] = (byte) 0xAB;
						data[1] = (byte) 0;
						data[2] = (byte) 11;
						data[3] = (byte) 0xff;
						data[4] = (byte) 0x93;
						data[5] = (byte) 0x80;
				//        data[6] = (byte)0;
						data[7] = (byte) ((year & 0xff00) >> 8);
						data[8] = (byte) (year & 0xff);
						data[9] = (byte) (month & 0xff);
						data[10] = (byte) (day & 0xff);
						data[11] = (byte) (hour & 0xff);
						data[12] = (byte) (minute & 0xff);
						data[13] = (byte) (second & 0xff);
						
						
					这些是你在同步时间的时候应该发送给手环的数据，如果发送成功，你会发现你的手环上的时间已经和手机上的时间保持一致了。 
		

		同步数据（ID=51）:
							现在时间已经同步了。 是时候获取手环上的数据了。
							在获取手环数据之前，你应该知道手环上有什么类型的数据。
							1.当前计步、卡路里、睡眠数据：
								手环当前的计步、卡路里、睡眠数据
							2.手环单机测量心率、血氧、血压数据：
								点击手环的按钮，例如进入手环上面的心率测量界面，这个时候就开始测量心率了。测量完成后手环会震动，并且将数据储存在手环中。
							
							3.整点测量数据（计步、卡路里、心率、血氧、血压、睡眠时间）
								要想手环每到整点就记录一组数据（计步、卡路里、心率、血氧、血压、睡眠），必须先开启手环的整点测量功能
											开启整点测量方法如下：
											向手环发送下面的数据
													byte[] bytes = new byte[7];
													bytes[0] = (byte) 0xAB;
													bytes[1] = (byte) 0;
													bytes[2] = (byte) 4;
													bytes[3] = (byte) 0xFF;
													bytes[4] = (byte) 0x78;
													bytes[5] = (byte) 0x80;
													bytes[6] = (byte) control;  // control=0 关闭整点测量 control=1 开启整点测量
													
											如果手环已经开启了整点测量，并且用户戴上手环，手环就会每次到整点都会测量并记录下一组数据
											
							
							
							同步数据：同步数据的时候你要告诉手环你需要哪个时间点之后的数据，所以你要发一个时间给手环。这是同步数据最重要的一件事。如果你不发时间过去，你每次调用同步数据的指令，手环都要把它所有的数据给你，这样很浪费。
							
							同步数据需要发的指令：
													
													Calendar calendar = Calendar.getInstance();
													//这个时间就是你需要传入的时间，用来告诉手环你需要这个时间点之后的数据，这个时间点之前的数据你不需要了，或许你已经储存在了你的数据库中了。
													calendar.setTimeInMillis(timeInMillis);
													int year = calendar.get(Calendar.YEAR);
													int month = calendar.get(Calendar.MONTH) + 1;
													int day = calendar.get(Calendar.DAY_OF_MONTH);
													int hour = calendar.get(Calendar.HOUR_OF_DAY);
													int minute = calendar.get(Calendar.MINUTE);
													int second = calendar.get(Calendar.SECOND);
													
													byte[] data = new byte[12];
													data[0] = (byte) 0xAB;
													data[1] = (byte) 0;
													data[2] = (byte) 9;
													data[3] = (byte) 0xff;
													data[4] = (byte) 0x51;  //ID
													data[5] = (byte) 0x80;
											//        data[6] = (byte)0;
													data[7] = (byte) ((year - 2000));
													data[8] = (byte) month;
													data[9] = (byte) day;
													data[10] = (byte) hour;
													data[11] = (byte) minute;
							
							一旦你发出这个指令，手环接收到了之后，手环就会给你发送数据。接下来要做的是解析手环返回来的数据。
							
							
							
							解析数据：
									直接上代码
									
									final byte[] txValue = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
									Log.i(TAG, "Received command" + DataHandlerUtils.bytesToHexStr(txValue));
									List<Integer> datas = DataHandlerUtils.bytesToArrayList(txValue);
									Log.i(TAG, datas.toString());

									//1.当前计步、卡路里、睡眠数据
									if (datas.get(0) == 0xAB && datas.get(4) == 0x51 && datas.get(5) == 0x08) {
										Log.d(TAG,"steps calories and sleep data current");
										//计步
										int steps = (datas.get(6) << 16) + (datas.get(7) << 8) + datas.get(8);
										float distance = (steps * 0.7f)/1000;//If the user does not tell you his stride, by default he walked 0.7m every step
										//卡路里
										int calories =(datas.get(9) << 16) + (datas.get(10) << 8) + datas.get(11);
										//睡眠
										long shallowSleep = (datas.get(12) * 60 + datas.get(13)) * 60 * 1000L;
										long deepSleep = (datas.get(14) * 60 + datas.get(15)) * 60 * 1000L;
										long sleepTime = shallowSleep+deepSleep;
										int wake_times = datas.get(16)
										

									}
									
									//2.手环单机测量心率、血氧、血压数据
									if ((datas.get(0) == 0xAB && datas.get(4) == 0x51)){
										int year = datas.get(6) + 2000;
										int month = datas.get(7);
										int day = datas.get(8);
										int hour = datas.get(9);
										int min = datas.get(10);
										String time= year+"-"+month+"-"+day+" "+hour+":"+min;
										Log.i(TAG,"hourly_time  "+time);
										SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
										long timeInMillis=0;
										try {
											Date date = sdf.parse(time);
											timeInMillis=date.getTime();
										} catch (ParseException e) {
											e.printStackTrace();
										}
										if (datas.get(5) == 0x11){
											Log.d(TAG,"the Heart rate data from band measure");
											//心率
											int hrValue = datas.get(11);                    

										}else if (datas.get(5) == 0x12){
											Log.d(TAG,"the Blood oxygen data from band measure");
											//血氧
											int bloodOxygen = datas.get(11);                      

										}else if (datas.get(5) == 0x14){
											Log.d(TAG,"the Blood pressure data from band measure");
											//血压数据
											int bloodPressureHigh= datas.get(11);
											int bloodPressureLow = datas.get(12);
											

										}
									}
									//3.整点测量数据（计步、卡路里、心率、血氧、血压、睡眠时间）
									if (datas.get(0) == 0xAB && datas.get(4) == 0x51 && datas.get(5) == 0x20){//Hourly
										Log.d(TAG,"the steps, calories, heart rate, blood oxygen,blood pressure data from hourly measure");
										int year = datas.get(6) + 2000;
										int month = datas.get(7);
										int day = datas.get(8);
										//注意：收到整点数据，我们要加一个小时
										int hour = datas.get(9)+1;
										String time= year+"-"+month+"-"+day+" "+hour;
										Log.i(TAG,"hourly_time  "+time);
										SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH");
										long timeInMillis=0;
										try {
											Date date = sdf.parse(time);
											timeInMillis=date.getTime();
										} catch (ParseException e) {
											e.printStackTrace();
										}
										SPUtils.putLong(mContext, SPUtils.HOURLY_MEASURETIME, timeInMillis);
										//计步
										int steps = (datas.get(10) << 16) + (datas.get(11) << 8) + datas.get(12);
										//卡路里
										int calories = (datas.get(13) << 16) + (datas.get(14) << 8) + datas.get(15);
										float distance = (steps * 0.7f)/1000;//If the user does not tell you his stride, by default he walked 0.7m every step
										
										BigDecimal bd = new BigDecimal((double) distance);
										BigDecimal bigDecimal = bd.setScale(2, RoundingMode.HALF_UP);
										float distance2 = bigDecimal.floatValue();
										//心率
										int heartRate = datas.get(16);
										//血氧
										int bloodOxygen = datas.get(17);
										//血压
										int bloodPressure_high = datas.get(18);
										int bloodPressure_low = datas.get(19);
									   

									}
									if (datas.get(0) == 0){
										Log.d(TAG,"second packet data from hourly measure");
										long timeInMillis=SPUtils.getLong(mContext,SPUtils.HOURLY_MEASURETIME,0);
										Log.i(TAG,"second packet---"+MyUtils.formatTime(timeInMillis,"yyyy-MM-dd HH:mm:ss"));
										//睡眠时间
										long  shwllow_time = datas.get(1) * 60 * 60 * 1000L + datas.get(2) * 60 * 1000L;
										long deep_time = datas.get(3) * 60 * 60 * 1000L + datas.get(4) * 60 * 1000L;
										long total_time = shwllow_time + deep_time;
										int wake_times = datas.get(5);
									 


									}
									
		单次测量、实时测量（ID=0x31）:
									单次测量：
											APP向手环发送一条开始测量的指令，手环开始测量（心率或血氧或血压）,45秒之后APP再发送关闭测量的指令，手环就会把测量结果返回给APP。（记得要发送关闭的指令，手环才会返回结果）
									实时测量：
											APP向手环发送一条开始测量的指令，手环开始测量（心率或血氧或血压），5s之后返回数据。之后每隔5秒返回一个数据，直到发送指令关闭测量。
		
		
									
										发送指令；
												byte[] bytes = new byte[7];
												bytes[0] = (byte) 0xAB;
												bytes[1] = (byte) 0;
												bytes[2] = (byte) 4;
												bytes[3] = (byte) 0xFF;
												bytes[4] = (byte) 0x31;//ID
												bytes[5] = (byte) status;//心率：0X09(单次) 0X0A(实时)/  血氧：0X11(单次) 0X12(实时)/  血压：0X21(单次) 0X22(实时)
												bytes[6] = (byte) control;//0 关 1 开
										
										
										解析数据：
										 if (datas.get(0) == 0xAB && datas.get(4) == 0x31) {
												  switch (datas.get(5)) {
													case 0x09:
														//心率（单次）
														int heartRate = datas.get(6);
													
														break;
													case 0x11:
														//血氧（单次）
														int bloodOxygen = datas.get(6);
													
														break;
													case 0x21:
														//血压（单次）
														int bloodPressureHigh = datas.get(6);
														int bloodPressureLow = datas.get(7);
													
														break;
														
													
													case 0X0A:
														//心率（实时）
														int heartRate = datas.get(6);
													
														break;
													case 0x12:
														//血氧（实时）
														int bloodOxygen = datas.get(6);
													
														break;
													case 0x22:
														//血压（实时）
														int bloodPressureHigh = datas.get(6);
														int bloodPressureLow = datas.get(7);
													
														break;
												}
														 

										 }		
					
								
		一键测量（ID=0x32）:
							一键测量：如果想在你的APP里面集成一键测量的功能，你发一键测量的指令给手环，手环收到指令之后就会开始测量心率、血压、血氧数据。并且在测量完成之后返回测量结果。（请在开启一分钟之后发送关闭测量的指令）
								发送指令；
										byte[] bytes = new byte[7];
										bytes[0] = (byte) 0xAB;
										bytes[1] = (byte) 0;
										bytes[2] = (byte) 4;
										bytes[3] = (byte) 0xFF;
										bytes[4] = (byte) 0x32;
										bytes[5] = (byte) 0x80;
										bytes[6] = (byte)control; //0关闭测量 1开启测量
										
								解析数据:
										if (datas.get(0) == 0xAB && datas.get(4) == 0x32) {
											int heartRate = datas.get(6);
											int bloodOxygen = datas.get(7);
											int bloodPressureHigh = datas.get(8);
											int bloodPressureLow = datas.get(9);
										
										}
		入睡时间记录（ID=0x52）:
								通过手环发过来的数据，计算出每一个时间段的睡眠状态是处于深睡、浅睡、或者醒来。要获取每一段的睡眠数据，需要向手环发送同步睡眠数据的指令，和同步其他数据一样，同样要一个时间点给手环。手环会把这个时间点之后的数据发送过来。
								
								发送指令：
									Calendar calendar = Calendar.getInstance();
									calendar.setTimeInMillis(timeInMillis);
									int year = calendar.get(Calendar.YEAR);
									int month = calendar.get(Calendar.MONTH) + 1;
									int day = calendar.get(Calendar.DAY_OF_MONTH);
									int hour = calendar.get(Calendar.HOUR_OF_DAY);
									int minute = calendar.get(Calendar.MINUTE);
																								
									byte[] data = new byte[12];
									data[0] = (byte) 0xAB;
									data[1] = (byte) 0;
									data[2] = (byte) 9;
									data[3] = (byte) 0xff;
									data[4] = (byte) 0x52;
									data[5] = (byte) 0x80;
							//        data[6] = (byte)0;
									data[7] = (byte) ((year - 2000));
									data[8] = (byte) month;
									data[9] = (byte) day;
									data[10] = (byte) hour;
									data[11] = (byte) minute;
									
								解析数据：
									if (datas.get(0) == 0xAB && datas.get(4) == 0X52) {
										int year = datas.get(6) + 2000;
										int month = datas.get(7);
										int day = datas.get(8);
										int hour = datas.get(9);
										int minute = datas.get(10);
										int sleepId = datas.get(11);//sleepId = 1 --> 一段睡眠    sleepId = 2 --> 深睡
										int time = datas.get(12) * 16 * 16 + datas.get(13);
									
									
									}
          你可以根据手环发过来的year,month,day,hour,minute，来确定这一条睡眠记录的开始时间，然后你可以根据time来确定这条睡眠记录的结束时间。sleepId 告诉你了记录的类型。sleepId = 1 --> 一段睡眠    sleepId = 2 --> 深睡。所以你可以得到类似这样的一条睡眠记录 sleepId=1 startTime=22:00 endTime=23:30 time=90 。当数据同步完成时，你会得到很多条这样的睡眠记录。sleepId = 2 的睡眠记录是深睡，你现在还需要知道有哪些时间段是浅睡和哪些时间段是醒来。我们这样定义醒来的时间段，按照时间先后顺序如果两条相邻的sleepId=1的睡眠记录没有连接起来，那么没有连接的起来的那段时间就是醒来。例如：
									
									//这两条记录第一条记录的endTime 和 第二条记录的startTime 都是23:30 所以它们是连接起来的
									sleepId=1 startTime=22:00 endTime=23:30 time=90 ;  
									sleepId=1 startTime=23:30 endTime=23:50 time=20 ;
									
									
									//这两条记录没有连接起来，第一条记录的endTime23:30 而第二条记录的startTime 都是23:35 
									sleepId=1 startTime=22:00 endTime=23:30 time=90 ;  
									sleepId=1 startTime=23:35 endTime=23:50 time=15 ;
									
									//所以产生了醒来时间 
									sleepId=0 startTime=23:30 endTime=23:35 time=5 ;（假如我们定义醒来的sleepId=0）
									
									现在你可以计算出醒来的时间段了，也已经知道深睡的时间段了。那么总的睡眠时间段减去醒来时间段和深睡时间段就是浅睡的时间段。
									
									
									
									
								
