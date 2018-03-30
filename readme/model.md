 ### Model层具体含义
 
 lib 需要使用指定的 model 字典存储音乐相关信息。具体的类是 `SongInfo`。
 
 #### 各个字段的含义说明
 
 | 字段名      |    类型 | 说明  |
 | :-------- | :--------:| :-- |
 | songId  | String |  音乐id   |
 | songName  | String |  音乐标题   |
 | songCover  | String |  音乐封面   |
 | songHDCover  | String |  专辑封面(高清)   |
 | songSquareCover  | String |  专辑封面(正方形)   |
 | songRectCover  | String |  专辑封面(矩形)   |
 | songRoundCover  | String |  专辑封面(圆形)   |
 | songCoverBitmap  | Bitmap |  封面的Bitmap，lib内部处理，不用管   |
 | songUrl  | String |  音乐播放地址   |
 | genre  | String |  类型（流派）   |
 | type  | String |  类型   |
 | size  | String |  音乐大小（默认“0”）   |
 | duration  | long |  音乐长度   |
 | artist  | String |  音乐艺术家   |
 | artistId  | String |  音乐艺术家id   |
 | downloadUrl  | String |  音乐下载地址   |
 | site  | String |  地点   |
 | favorites  | int |  喜欢数（默认0）   |
 | playCount  | int |  播放数（默认0）   |
 | trackNumber  | int |  媒体的曲目号码（序号：1234567……）   |
 | language  | String |  语言   |
 | country  | String |  国家   |
 | proxyCompany  | String |  代理公司   |
 | publishTime  | String |  发布时间   |
 | description  | String |  音乐描述   |
 | versions  | String |  音乐版本   |
 | albumInfo  | AlbumInfo |  专辑信息   |
 | tempInfo  | TempInfo |  其他信息   |****
 
 
 #### AlbumInfo 专辑信息字段说明
   
  | 字段名      |    类型 | 说明  |
  | :-------- | :--------:| :--  |
  | albumId  | String |  专辑id   |
  | albumName  | String |  专辑名称   |
  | albumCover  | String |  专辑封面   |
  | albumHDCover  | String |  专辑封面(高清)   |
  | albumSquareCover  | String |  专辑封面(正方形)   |
  | albumRectCover  | String | 专辑封面(矩形)   |
  | albumRoundCover  | String | 专辑封面(圆形)   |
  | artist  | String | 专辑艺术家   |
  | songCount  | int | 专辑音乐数（默认0）   |
  | playCount  | int | 专辑播放数（默认0）   |
  
   
 #### TempInfo 其他信息字段说明
 
 这个为其他信息，用于上面字段都没有你需要的或者当你需要一些临时字段的时候，不过最好别用，因为容易混乱，如果上面说到的字段都没有你需要的，请告诉我，我加上去。
 
   | 字段名      |    类型 | 说明  |
   | :--------: | :--------:| :--: |
   | temp_1  | String |  临时字段   |
   | temp_2  | String |  临时字段   |
   | temp_3  | String |  临时字段   |
   | temp_4  | String |  临时字段   |
   | temp_5  | String |  临时字段   |
   | temp_6  | String |  临时字段   |
   | temp_7  | String |  临时字段   |
   | temp_8  | String |  临时字段   |
   | temp_9  | String |  临时字段   |
 
 
       
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
   
 