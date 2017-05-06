package com.voxelbusters.nativeplugins.extensions;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.util.Arrays;

public class FileProviderExtended extends FileProvider
{

	@Override
	public Cursor query(Uri uri, String[] columnProjection, String selection, String[] selectionArguments, String sortingOrder)
	{
		Cursor source = super.query(uri, columnProjection, selection, selectionArguments, sortingOrder);

		String[] sourceColumnNames = source.getColumnNames();
		String[] finalColumnNames = null;

		for (String columnName : sourceColumnNames)
		{
			if (MediaStore.MediaColumns.DATA.equals(columnName))
			{
				finalColumnNames = sourceColumnNames;
			}
		}

		if (finalColumnNames == null)
		{
			finalColumnNames = Arrays.copyOf(sourceColumnNames, sourceColumnNames.length + 1);
			//Add at end
			finalColumnNames[sourceColumnNames.length] = MediaStore.MediaColumns.DATA;
		}

		MatrixCursor cursor = new MatrixCursor(finalColumnNames, source.getCount());

		source.moveToPosition(-1);

		while (source.moveToNext())
		{
			MatrixCursor.RowBuilder row = cursor.newRow();
			for (int i = 0; i < sourceColumnNames.length; i++)
			{
				row.add(source.getString(i));
			}
		}

		return cursor;
	}

}
