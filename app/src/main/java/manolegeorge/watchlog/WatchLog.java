package manolegeorge.watchlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class WatchLog {

    public static String implode(String glue, ArrayList<String> pieces) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < pieces.size(); i++) {
            sb.append(pieces.get(i));
            if(i < pieces.size() - 1) sb.append(glue);
        }
        return sb.toString();
    }

    public static Bitmap getCircledBitmap(Bitmap bitmap) {

        if(bitmap != null) {

            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

            return output;

        }

        return null;

    }

    public static String bitmapToBase64(Bitmap bitmap, String extension) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if(extension.equals("jpg") || extension.equals("jpeg")) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        } else if(extension.equals("png")) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        } else {
            return null;
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static com.nostra13.universalimageloader.core.ImageLoader ImageLoader(Context context) {
        File cacheDir = StorageUtils.getCacheDirectory(context);
        com.nostra13.universalimageloader.core.ImageLoader imageLoader = com.nostra13.universalimageloader.core.ImageLoader.getInstance();
        ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(context)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .diskCacheSize(50 * 1024 * 1024)
                .build();
        imageLoader.init(imageLoaderConfiguration);
        return imageLoader;
    }

    public static DisplayImageOptions getImageLoaderOptions() {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.no_poster)
                .showImageForEmptyUri(R.drawable.no_poster)
                .showImageOnFail(R.drawable.no_poster)
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .build();
    }

    public static class BlurBuilder {

        private static final float BITMAP_SCALE = 1f;
        private static final float BLUR_RADIUS = 15f;

        public static Bitmap blur(Context context, Bitmap image) {

            int width = Math.round(image.getWidth() * BITMAP_SCALE);
            int height = Math.round(image.getHeight() * BITMAP_SCALE);

            Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
            Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

            RenderScript rs = RenderScript.create(context);
            ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
            Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
            theIntrinsic.setRadius(BLUR_RADIUS);
            theIntrinsic.setInput(tmpIn);
            theIntrinsic.forEach(tmpOut);
            tmpOut.copyTo(outputBitmap);

            return outputBitmap;

        }

    }

    public static class Utils {

        public static void fadeOut(View view) {
            WatchLog.Utils.fadeOut(view, 250);
        }

        public static void fadeOut(final View view, int duration) {
            AlphaAnimation animation = new AlphaAnimation(1f, 0f);
            animation.setInterpolator(new LinearInterpolator());
            animation.setDuration(duration);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    view.setVisibility(View.VISIBLE);
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.GONE);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            view.startAnimation(animation);
        }

        public static void fadeIn(View view) {
            WatchLog.Utils.fadeIn(view, 200);
        }

        public static void fadeIn(final View view, int duration) {
            AlphaAnimation animation = new AlphaAnimation(0f, 1f);
            animation.setInterpolator(new LinearInterpolator());
            animation.setDuration(duration);
            view.setVisibility(View.VISIBLE);
            view.startAnimation(animation);
        }

        public static String getProfilePicture(SharedPreferences sharedPreferences) {
            return sharedPreferences.getString("profile_picture", "undefined");
        }

        public static Bitmap getProfilePictureRounded(SharedPreferences sharedPreferences) {
            try {
                byte[] decodedProfilePicture = Base64.decode(sharedPreferences.getString("profile_picture", "undefined"), Base64.DEFAULT);
                return WatchLog.getCircledBitmap(BitmapFactory.decodeByteArray(decodedProfilePicture, 0, decodedProfilePicture.length));
            } catch(IllegalArgumentException e) {
                return null;
            }
        }

        public static boolean hasInternetConnection(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
            return false;
        }

        public static Bitmap base64ToBitmap(String base64) {
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }

        public static String getDate(long time, String formatter) {
            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
            calendar.setTimeInMillis(time * 1000);
            return DateFormat.format(formatter, calendar).toString();
        }

    }

}
