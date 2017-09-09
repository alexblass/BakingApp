package net.alexblass.bakingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import net.alexblass.bakingapp.models.RecipeStep;

import static net.alexblass.bakingapp.RecipeDetailFragment.RECIPE_STEP_KEY;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {RecipeStepFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the { RecipeStepFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//public class RecipeStepFragment extends Fragment {
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    private OnFragmentInteractionListener mListener;
//
//    public RecipeStepFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment RecipeStepFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static RecipeStepFragment newInstance(String param1, String param2) {
//        RecipeStepFragment fragment = new RecipeStepFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_recipe_step, container, false);
//    }
//
//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
//}
public class RecipeStepFragment extends Fragment implements ExoPlayer.EventListener {
    // The selected RecipeStep
    private RecipeStep mSelectedStep;

    // The views in the StepDetail layout
    private TextView mTitleTv, mDescriptionTv;
    private ImageView mThumbnailImageView;
    private SimpleExoPlayer mExoPlayer;
    private SimpleExoPlayerView mPlayerView;
    private ProgressBar mLoadingIndicator;



    // Empty constructor
    public RecipeStepFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recipe_step, container, false);

        Intent intentThatStartedThisActivity = getActivity().getIntent();

        if (intentThatStartedThisActivity != null) {
            // If there's a valid RecipeStep, get the data from the RecipeStep and display it
            if (intentThatStartedThisActivity.hasExtra(RECIPE_STEP_KEY)) {
                mSelectedStep = intentThatStartedThisActivity.getParcelableExtra(RECIPE_STEP_KEY);

                mThumbnailImageView = (ImageView) rootView.findViewById(R.id.step_thumbnail_imageview);

                mTitleTv = (TextView) rootView.findViewById(R.id.step_description_title_tv);
                mDescriptionTv = (TextView) rootView.findViewById(R.id.step_description_tv);
                mPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.step_video_exoplayer);
                mLoadingIndicator = (ProgressBar) rootView.findViewById(R.id.loading_indicator_step_detail);

                BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                TrackSelection.Factory videoTrackSelectionFactory =
                        new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
                TrackSelector trackSelector =
                        new DefaultTrackSelector(videoTrackSelectionFactory);
                LoadControl loadControl = new DefaultLoadControl();

                mExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector, loadControl);

                mPlayerView.setPlayer(mExoPlayer);
                mPlayerView.setKeepScreenOn(true);

                DataSource.Factory dataSourceFactory =
                        new DefaultDataSourceFactory(getActivity(), Util.getUserAgent(getActivity(), "ExoPlayer"));

                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

                mTitleTv.setText(mSelectedStep.getShortDescription());
                mDescriptionTv.setText(mSelectedStep.getDescription());

                // Check if there is an image to the step
                if (!mSelectedStep.getImageUrl().equals("")){
                    mLoadingIndicator.setVisibility(View.GONE);
                    mThumbnailImageView.setVisibility(View.VISIBLE);
                    Picasso.with(getActivity())
                            .load(mSelectedStep.getImageUrl())
                            .into(mThumbnailImageView);
                }

                // Check if there is a video to the step
                if (!mSelectedStep.getVideoUrl().equals("")){
                    Uri vidUri = Uri.parse(mSelectedStep.getVideoUrl());

                    MediaSource videoSource = new ExtractorMediaSource(vidUri,
                            dataSourceFactory, extractorsFactory, null, null);

                    mExoPlayer.addListener(this);
                    mExoPlayer.prepare(videoSource);
                    mExoPlayer.setPlayWhenReady(true);
                }

                // If there's no video or image, hide the loading indicator
                if (mSelectedStep.getImageUrl().equals("") && mSelectedStep.getVideoUrl().equals("")){
                    mLoadingIndicator.setVisibility(View.GONE);
                }
            }
        }
        return rootView;
    }

    // Pause the video when the player is not in focus
    @Override
    public void onPause() {
        super.onPause();
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
        }
    }

    // Required override method
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    // Required override method
    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    // The ExoPlayer video states
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                break;
            case ExoPlayer.STATE_IDLE:
                break;
            case ExoPlayer.STATE_READY:
                // When the video is ready to play, make it visible
                mLoadingIndicator.setVisibility(View.GONE);
                mPlayerView.setVisibility(View.VISIBLE);
                break;
            case ExoPlayer.STATE_ENDED:
                break;
        }
    }

    // An error message if there's a problem streaming the video
    @Override
    public void onPlayerError(ExoPlaybackException error) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle(R.string.video_error_title);
        dialog.setMessage(R.string.video_error_body);
        dialog.setPositiveButton(R.string.positive_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog ad = dialog.create();
        ad.show();
    }

    // Required Override method
    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExoPlayer.release();
    }
}