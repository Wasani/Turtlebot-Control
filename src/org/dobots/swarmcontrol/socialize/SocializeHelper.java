package org.dobots.swarmcontrol.socialize;

import org.dobots.swarmcontrol.R;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.Utils;
import org.dobots.utilities.log.Loggable;

import robots.RobotType;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.socialize.CommentUtils;
import com.socialize.LikeUtils;
import com.socialize.ShareUtils;
import com.socialize.ViewUtils;
import com.socialize.entity.Entity;
import com.socialize.entity.Like;
import com.socialize.error.SocializeException;
import com.socialize.listener.like.LikeAddListener;
import com.socialize.listener.like.LikeDeleteListener;
import com.socialize.listener.view.ViewAddListener;

public class SocializeHelper extends Loggable {

	private static final String TAG = "SocializeHelper";

	public interface IChangeEventListener {
		public void onChange();
	}

	public interface ILikeEventListener {
		public void onLike();

		public void onUnlike();
	}

	// ----------------------------------------------------------------------------------------
	public static void setupActionBar(final BaseActivity i_oContext,
			final Entity i_oEntity, final ILikeEventListener i_oListener) {

		final CheckBox btnCustomCheckBoxLike = (CheckBox) i_oContext
				.findViewById(R.id.btnCustomCheckBoxLike);

		// // Make the button a socialize like button!
		// LikeUtils.makeLikeButton(i_oContext, btnCustomCheckBoxLike,
		// i_oEntity, new LikeButtonListener() {
		//
		// boolean clicked = false;
		//
		// @Override
		// public void onClick(CompoundButton button) {
		// // You can use this callback to set any loading text or display a
		// progress dialog
		//
		// // set click flag to true to indicate that the change comes from a
		// button click
		// clicked = true;
		// }
		//
		// @Override
		// public void onCheckedChanged(CompoundButton button, boolean
		// isChecked) {
		// // The like was posted successfully, change the button to reflect the
		// change
		// updateLikeButton(button);
		// }
		//
		// @Override
		// public void onError(CompoundButton button, Exception error) {
		// // An error occurred posting the like, we need to return the button
		// to its original state
		// Log.e("Socialize", "Error on like button", error);
		// updateLikeButton(button);
		// }
		//
		// private void updateLikeButton(CompoundButton i_btnButton) {
		// // update the button text
		// if(i_btnButton.isChecked()) {
		// i_btnButton.setText("Liked");
		// }
		// else {
		// i_btnButton.setText("Like");
		// }
		//
		// // inform the listener about the change
		// if (i_oListener != null && clicked) {
		// i_oListener.onChange();
		// }
		//
		// // reset the click flag
		// clicked = false;
		// }
		//
		// });

		final boolean clicked = false;
		btnCustomCheckBoxLike.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final CheckBox cbxLike = (CheckBox) v;

				// isChecked is set before the click event
				if (!cbxLike.isChecked()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							i_oContext);
					builder.setMessage("Are you sure you want to unlike?")
							.setCancelable(false)
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											LikeUtils.unlike(i_oContext,
													i_oEntity.getKey(),
													new LikeDeleteListener() {

														@Override
														public void onDelete() {
															cbxLike.setChecked(false);
															// i_oListener.onChange();
															i_oListener
																	.onUnlike();
														}

														@Override
														public void onError(
																SocializeException arg0) {
															// cbxLike.setChecked(false);
															// i_oListener.onChange();
														}

													});
											dialog.dismiss();
										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											cbxLike.setChecked(true);
											dialog.cancel();
										}
									});
					builder.create().show();
				} else {
					LikeUtils.like(i_oContext, i_oEntity,
							new LikeAddListener() {

								@Override
								public void onCancel() {
									cbxLike.setChecked(false);
									super.onCancel();
								}

								@Override
								public void onCreate(Like arg0) {
									cbxLike.setChecked(true);
									// i_oListener.onChange();
									i_oListener.onLike();
								}

								@Override
								public void onError(SocializeException arg0) {
									// cbxLike.setChecked(true);
									// i_oListener.onChange();
								}

							});
				}
			}
		});

		btnCustomCheckBoxLike
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							buttonView.setText("Liked");
						} else {
							buttonView.setText("Like");
						}
					}
				});

		Utils.runAsyncUiTask(new Runnable() {

			@Override
			public void run() {
				btnCustomCheckBoxLike.setChecked(i_oEntity.getUserEntityStats()
						.isLiked());
			}
		});

		CheckBox btnCustomCheckBoxShare = (CheckBox) i_oContext
				.findViewById(R.id.btnCustomCheckBoxShare);
		btnCustomCheckBoxShare.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ShareUtils.showShareDialog(i_oContext, i_oEntity);
			}
		});

		CheckBox btnCustomCheckBoxComment = (CheckBox) i_oContext
				.findViewById(R.id.btnCustomCheckBoxComment);
		setupComments(i_oContext, i_oEntity, btnCustomCheckBoxComment);

		// if (!isDebuggable) {

		// }

	}

	// ----------------------------------------------------------------------------------------
	// async call
	public static void registerRobotView(final BaseActivity i_oContext,
			final RobotType i_eRobot) {
		Utils.runAsyncTask(new Runnable() {

			@Override
			public void run() {
				Entity oEntity = SocializeEntityHelper.getRobotEntity(
						i_oContext, i_eRobot);
				if (oEntity != null) {
					registerView(i_oContext, oEntity);
				}
			}
		});
	}

	// ----------------------------------------------------------------------------------------
	// async call
	public static void registerMainView(final BaseActivity i_oContext) {
		Utils.runAsyncTask(new Runnable() {

			@Override
			public void run() {
				Entity oEntity = SocializeEntityHelper
						.getMainEntity(i_oContext);
				if (oEntity != null) {
					registerView(i_oContext, oEntity);
				}
			}
		});
	}

	// ----------------------------------------------------------------------------------------
	// blocking call
	private static void registerView(final BaseActivity i_oContext,
			final Entity i_oEntity) {
		 boolean bIsDebugVersion = Utils.isDebugVersion(i_oContext);
//		boolean bIsDebugVersion = false;

		// avoid registering views while in debug mode, otherwise the statistics
		// of getsocialize will be
		// corrupted. only register views in the release version
		if (!bIsDebugVersion) {
			ViewUtils.view(i_oContext, i_oEntity, new ViewAddListener() {

				@Override
				public void onError(SocializeException arg0) {
				}

				@Override
				public void onCreate(com.socialize.entity.View arg0) {
					Log.i(TAG, "view registered");
				}
			});
		}
	}

	// Comments
	// ==============================================================================

	// ----------------------------------------------------------------------------------------
	public static void setupComments(final BaseActivity i_oContext,
			final Entity i_oEntity, CompoundButton i_oButton) {

		i_oButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CommentUtils.showCommentView(i_oContext, i_oEntity);
			}
		});

	}

	// ----------------------------------------------------------------------------------------
	public static void setupComments(final BaseActivity activity, final RobotType i_eRobot) {
		
		final Handler oUiHandler = new Handler(activity.getMainLooper());
		
		LinearLayout layTitle = (LinearLayout) activity.findViewById(R.id.layTitle);
		if (layTitle == null) {
			return;
		}
		
		TextView spacer = new TextView(activity);
		spacer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1f));
		layTitle.addView(spacer);
		
		final CheckBox cbxComments = new CheckBox(activity);
		cbxComments.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		cbxComments.setButtonDrawable(R.drawable.comment);
		layTitle.addView(cbxComments);

		cbxComments.setVisibility(View.INVISIBLE);
		new Thread(new Runnable() {
			public void run() {
				Entity oEntity = SocializeEntityHelper.getRobotEntity(activity, i_eRobot);
				if (oEntity != null) {
					setupComments(activity, oEntity, cbxComments);
					oUiHandler.post(new Runnable() {
						
						@Override
						public void run() {
							cbxComments.setVisibility(View.VISIBLE);
						}
					});
				}
			}
		}).start();
	}
}
