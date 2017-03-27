package com.voxelbusters.nativeplugins.utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.voxelbusters.nativeplugins.defines.CommonDefines;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtility
{
	public final static int	IMAGE_QUALITY	= 100;	// TODO

	public static String getContentsOfFile(String filePath)
	{
		String dataString = null;
		BufferedReader bufferedReader = null;
		try
		{
			bufferedReader = new BufferedReader(new FileReader(filePath));

			if (bufferedReader != null)
			{
				String eachLine;

				while ((eachLine = bufferedReader.readLine()) != null)
				{
					dataString += eachLine;
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// Close the bufferReader before leaving if its opened
		if (bufferedReader != null)
		{
			try
			{
				bufferedReader.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return dataString;
	}

	public static Uri getSavedFileUri(byte[] data, int length, File destinationDir, String destinationFileName, boolean addScheme)
	{
		String filePath = getSavedFile(data, length, destinationDir, destinationFileName, addScheme, true);

		return Uri.fromFile(new File(filePath));
	}

	public static void replaceFile(byte[] data, File destinationDir, String destinationFileName)
	{
		if (data != null)
		{
			getSavedFile(data, data.length, destinationDir, destinationFileName, false);
		}
		else
		{
			File destinationFile = new File(destinationDir, destinationFileName);
			if (destinationFile.exists())
			{
				destinationFile.delete();
			}
		}
	}

	public static String getSavedFile(byte[] data, int length, File destinationDir, String destinationFileName, boolean addScheme)
	{
		return getSavedFile(data, length, destinationDir, destinationFileName, addScheme, true);
	}

	public static String getSavedFile(byte[] data, int length, File destinationDir, String destinationFileName, boolean addScheme, boolean needsGlobalAccess)
	{
		String destPath = null;

		if ((data != null) && (length > 0))
		{
			//Check a head and create if it doesn't exist
			createDirectoriesIfUnAvailable(destinationDir.getAbsolutePath());

			File destinationFile = new File(destinationDir, destinationFileName);

			if (destinationFile.exists())
			{
				destinationFile.delete();

				try
				{
					destinationFile.createNewFile();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

			}

			if (needsGlobalAccess)
			{
				destinationFile.setReadable(true, false);
				destinationFile.setWritable(true, false);
			}

			FileOutputStream outputStream = null;

			try
			{
				outputStream = new FileOutputStream(destinationFile);
				outputStream.write(data);
				outputStream.close();
				destPath = destinationFile.getAbsolutePath();
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			if ((destPath != null) && addScheme)
			{
				destPath = "file://" + destPath;
			}
		}

		return destPath;
	}

	public static void createDirectoriesIfUnAvailable(String dir)
	{
		File file = new File(dir);
		if (!file.exists())
		{
			file.mkdirs();
		}
	}

	public static ByteArrayOutputStream getBitmapStream(String path)
	{
		File file = new File(path);
		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, stream);

		return stream;
	}

	static void createPathIfUnAvailable(File destinationDir, File destinationFile)
	{
		if (!destinationFile.exists())
		{
			try
			{
				destinationDir.mkdirs();
				destinationFile.createNewFile();
			}
			catch (IOException e)
			{
				Debug.error(CommonDefines.FILE_UTILS_TAG, "Creating file failed!");
				e.printStackTrace();
			}
		}
	}

	public static String createFileFromStream(InputStream stream, File destinationDir, String destinationFileName)
	{
		String absoluteDestinationPath = null;
		File destinationFile = new File(destinationDir, destinationFileName);

		createPathIfUnAvailable(destinationDir, destinationFile);

		try
		{
			OutputStream out = new FileOutputStream(destinationFile);
			byte[] buf = new byte[1024];
			int length;
			while ((length = stream.read(buf)) > 0)
			{
				out.write(buf, 0, length);
			}
			out.close();
			stream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		absoluteDestinationPath = destinationFile.getAbsolutePath();

		return absoluteDestinationPath;
	}

	public static String getScaledImagePathFromBitmap(Bitmap bitmap, File destinationDir, String destinationFileName, float scaleFactor)
	{
		String absoluteDestinationPath = null;

		File destinationImageFile = new File(destinationDir, destinationFileName);

		createPathIfUnAvailable(destinationDir, destinationImageFile);

		OutputStream outStream = null;

		try
		{
			outStream = new FileOutputStream(destinationImageFile);

			int width = 0;
			int height = 0;

			if (bitmap != null)
			{
				width = (int) (bitmap.getWidth() * scaleFactor);
				height = (int) (bitmap.getHeight() * scaleFactor);
			}

			if ((width != 0) && (height != 0))
			{
				// Below method returns same bitmap if no change in
				// width
				// and height (scaleFactor = 1.0f), which is nice!
				bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);

				// Save the created bitmap
				bitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, outStream);

				absoluteDestinationPath = destinationImageFile.getAbsolutePath();

			}
			else
			{
				Debug.error(CommonDefines.FILE_UTILS_TAG, "Width and height should be greater than zero. Returning null reference");
			}

		}
		catch (FileNotFoundException e)
		{
			Debug.error(CommonDefines.FILE_UTILS_TAG, "Error creating scaled bitmap " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (outStream != null)
				{
					// Dispose the stream
					outStream.flush();
					outStream.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return absoluteDestinationPath;
	}

	public static String getScaledImagePath(String sourcePath, File destinationDir, String destinationFileName, float scaleFactor, boolean deleteSource)
	{
		String absoluteDestinationPath = null;

		// Create a new bitmap based on the scale factor specified.Return
		// original if scale factor is 1.0f
		File sourceImageFile = new File(sourcePath);

		Bitmap bitmap = BitmapFactory.decodeFile(sourceImageFile.getAbsolutePath());

		absoluteDestinationPath = getScaledImagePathFromBitmap(bitmap, destinationDir, destinationFileName, scaleFactor);

		if (deleteSource)
		{
			sourceImageFile.delete();
		}

		return absoluteDestinationPath;
	}

	public static String getFilePathromURI(Context context, Uri uri)
	{
		File file = new File(uri.toString());
		return file.getAbsolutePath();
	}

	public static String getSavedLocalFileFromUri(Context context, Uri uri, String folderName, String targetFileName)
	{
		return getSavedFileFromUri(context, uri, context.getDir(folderName, Context.MODE_PRIVATE), targetFileName);
	}

	public static String getSavedFileFromUri(Context context, Uri uri, File targetDirectory, String targetFileName)
	{

		ByteArrayOutputStream byteStream = null;

		byte[] byteArray = null;

		ContentResolver resolver = context.getContentResolver();;
		try
		{
			InputStream inputStream = resolver.openInputStream(uri);
			byteStream = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];
			while ((inputStream.read(buffer)) != -1)
			{
				byteStream.write(buffer);
			}

			byteStream.flush();
			byteArray = byteStream.toByteArray();

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		if (byteStream != null)
		{
			try
			{
				byteStream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		if (byteArray != null)
		{
			return getSavedFile(byteArray, byteArray.length, targetDirectory, targetFileName, true);
		}
		else
		{
			return null;
		}
	}

	public static Uri createSharingFileUri(Context context, byte[] byteArray, int byteArrayLength, String dirName, String fileName)
	{

		boolean hasExternalDir = ApplicationUtility.hasExternalStorageWritable(context);

		// Create a bitmap and save it
		String imagePath = FileUtility.getSavedFile(byteArray, byteArrayLength, ApplicationUtility.getExternalTempDirectoryIfExists(context, dirName), fileName, false);

		Debug.log(CommonDefines.SHARING_TAG, "Saving temp at " + imagePath);
		Uri imageUri = null;

		if (!StringUtility.isNullOrEmpty(imagePath))
		{
			//Check if external directory exists. if so use that.
			if (!hasExternalDir)
			{
				imageUri = FileProvider.getUriForFile(context, ApplicationUtility.getFileProviderAuthoityName(context), new File(imagePath));
			}
			else
			{
				imageUri = Uri.fromFile(new File(imagePath));
			}

			context.grantUriPermission(ApplicationUtility.getPackageName(context), imageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}

		return imageUri;
	}
}
