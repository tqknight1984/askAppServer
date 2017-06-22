cd /D D:\SoftwareForCode\MyEclipseProject\TakeTaxiServer\protoc
protoc --java_out=../src ModelPrt.proto
copy D:\SoftwareForCode\MyEclipseProject\TakeTaxiServer\src\com\xiyuan\taketaxi\model\ModelPrt.java D:\SoftwareForCode\MyEclipseProject\TakeTaxiClient\src\com\xiyuan\taketaxi\model
pause