# PhotoGallery
This project is an app for Android platform. it is developed using Flickr Apis, mainly including such functions as following:

1. view, search and share public photos.
  By fetching xml data from the server and parse it into each image item. And then download the bitmaps using Http connection. To avoid
  causing OOM and downloading repeatedly from Internet, memory and disk LruCache were used. Also sharing images function is added, support
  to share up to 5 images to other apps.
  
 <img src="https://github.com/Grinfield/PhotoGallery/blob/master/project%20captures/1.png" width="200"/>
  <img src="https://github.com/Grinfield/PhotoGallery/blob/master/project%20captures/7.png" width="200"/>
   <img src="https://github.com/Grinfield/PhotoGallery/blob/master/project%20captures/4.png" width="200"/>
2. login to user own account, and after authorized by Flickr server, you can view your personal photos uploaded before. You can also shift
  to view public photos by yourself. You can also log out if you want and personal photos will not be visited.
  
 <img src="https://github.com/Grinfield/PhotoGallery/blob/master/project%20captures/8.png" width="200"/>
  <img src="https://github.com/Grinfield/PhotoGallery/blob/master/project%20captures/6.png" width="200"/>
   <img src="https://github.com/Grinfield/PhotoGallery/blob/master/project%20captures/2.png" width="200"/>
3. take and upload pictures to Flickr.
  This function is designed to take photos with camera by avoiding open your cellphone's camera app. After you take photos,
  you can upload your photos to your Flickr account.
  

  <img src="https://github.com/Grinfield/PhotoGallery/blob/master/project%20captures/5.png" width="200"/>
4. Using AlarmManager to send regular IntentService to check in the background whether latest images is uploaded from the server, if so, a notification will be showed in the status bar. However, when you are interact with this app, notification will be canceled.

 <img src="https://github.com/Grinfield/PhotoGallery/blob/master/project%20captures/3.png" width="200"/>
 
All these main functions are conveniently available, for they are integrated to 3 tabs in bottom navigation bar. You can switch to another between them.

