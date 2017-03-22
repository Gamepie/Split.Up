package com.voxelbusters.nativeplugins.features.medialibrary;

public interface IVideoPlayBackListener
{

	void onVideoPlayPaused();

	void onVideoPlayEnded();

	void onVideoPlayError(String description);

	void onVideoPlayUserExited();

}
