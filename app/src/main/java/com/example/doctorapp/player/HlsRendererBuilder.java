/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.doctorapp.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.os.Handler;

import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.hls.DefaultHlsTrackSelector;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.hls.PtsTimestampAdjusterProvider;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.Id3Parser;
import com.google.android.exoplayer.text.eia608.Eia608TrackRenderer;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.ManifestFetcher.ManifestCallback;

import java.io.IOException;
import java.util.List;

public class HlsRendererBuilder implements DemoPlayer.RendererBuilder {

  private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private static final int BUFFER_SEGMENTS = 256;

  private final Context context;
  private final String userAgent;
  private final String url;

  private AsyncRendererBuilder currentAsyncBuilder;

  public HlsRendererBuilder(Context context, String userAgent, String url) {
    this.context = context;
    this.userAgent = userAgent;
    this.url = url;
  }

  @Override
  public void buildRenderers(DemoPlayer player) {
    currentAsyncBuilder = new AsyncRendererBuilder(context, userAgent, url, player);
    currentAsyncBuilder.init();
  }

  @Override
  public void cancel() {
    if (currentAsyncBuilder != null) {
      currentAsyncBuilder.cancel();
      currentAsyncBuilder = null;
    }
  }

  private static final class AsyncRendererBuilder implements ManifestCallback<HlsPlaylist> {

    private final Context context;
    private final String userAgent;
    private final String url;
    private final DemoPlayer player;
    private final ManifestFetcher<HlsPlaylist> playlistFetcher;

    private boolean canceled;

    public AsyncRendererBuilder(Context context, String userAgent, String url, DemoPlayer player) {
      this.context = context;
      this.userAgent = userAgent;
      this.url = url;
      this.player = player;
      HlsPlaylistParser parser = new HlsPlaylistParser();
      playlistFetcher = new ManifestFetcher<>(url, new DefaultUriDataSource(context, userAgent),
          parser);
    }

    public void init() {
      playlistFetcher.singleLoad(player.getMainHandler().getLooper(), this);
    }

    public void cancel() {
      canceled = true;
    }

    @Override
    public void onSingleManifestError(IOException e) {
      if (canceled) {
        return;
      }

      player.onRenderersError(e);
    }

    @Override
    public void onSingleManifest(HlsPlaylist manifest) {
      if (canceled) {
        return;
      }

      Handler mainHandler = player.getMainHandler();
      LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(BUFFER_SEGMENT_SIZE));
      DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();


      DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);

      HlsChunkSource chunkSource = new HlsChunkSource(true, dataSource, manifest, DefaultHlsTrackSelector.newDefaultInstance(context), bandwidthMeter,
              new PtsTimestampAdjusterProvider(), HlsChunkSource.ADAPTIVE_MODE_SPLICE);

      HlsSampleSource sampleSource = new HlsSampleSource(chunkSource, loadControl,
          BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, mainHandler, player, com.appunite.appunitevideoplayer.player.DemoPlayer.TYPE_VIDEO);
      MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context,
              sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT, 5000, mainHandler, player, 50);
      MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, MediaCodecSelector.DEFAULT,
              null, true, player.getMainHandler(), player, AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);
      MetadataTrackRenderer<List<Id3Frame>> id3Renderer = new MetadataTrackRenderer<>(
          sampleSource, new Id3Parser(), player, mainHandler.getLooper());
      Eia608TrackRenderer closedCaptionRenderer = new Eia608TrackRenderer(sampleSource, player,
          mainHandler.getLooper());

      TrackRenderer[] renderers = new TrackRenderer[com.appunite.appunitevideoplayer.player.DemoPlayer.RENDERER_COUNT];
      renderers[com.appunite.appunitevideoplayer.player.DemoPlayer.TYPE_VIDEO] = videoRenderer;
      renderers[com.appunite.appunitevideoplayer.player.DemoPlayer.TYPE_AUDIO] = audioRenderer;
      renderers[com.appunite.appunitevideoplayer.player.DemoPlayer.TYPE_METADATA] = id3Renderer;
      renderers[DemoPlayer.TYPE_TEXT] = closedCaptionRenderer;
      player.onRenderers(renderers, bandwidthMeter);
    }

  }

}
