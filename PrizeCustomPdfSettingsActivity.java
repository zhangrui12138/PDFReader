/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.net.Uri;
import java.io.*;
import android.graphics.pdf.PdfRenderer;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;
import android.util.Log;
import android.graphics.Color;
import android.view.MotionEvent;


public class PrizeCustomPdfSettingsActivity extends Activity implements View.OnClickListener {

    
    private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";

    private static final String FILENAME = "SNK1958.pdf";

    private ParcelFileDescriptor mFileDescriptor;

    private PdfRenderer.Page mCurrentPage;

    private int mPageIndex;

    private PdfRenderer mPdfRenderer;
    private ImageView mImageView;
    private Button btnPrevious;
    private Button btnNext;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mCurrentPage) {
            outState.putInt(STATE_CURRENT_PAGE_INDEX, mCurrentPage.getIndex());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prize_pdf_reader);
        mImageView = (ImageView) findViewById(R.id.iv);
        btnPrevious = (Button) findViewById(R.id.btn_previous);
        btnNext = (Button) findViewById(R.id.btn_next);

        btnPrevious.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        mPageIndex = 0;
        if (null != savedInstanceState) {
            mPageIndex = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, 0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            openRenderer(this);
            showPage(mPageIndex);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    
    private void openRenderer(Context context) throws IOException {
        
        File file = new File(context.getFilesDir(), FILENAME);
        if (!file.exists()) {
            
            InputStream asset = getAssets().open(FILENAME);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
			//output.flush();
            asset.close();
            output.close();
        }
        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        
        if (mFileDescriptor != null) {
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        }
    }

    private void showPage(int index) {
        if (mPdfRenderer.getPageCount() <= index) {
            return;
        }
       
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        
        mCurrentPage = mPdfRenderer.openPage(index);
        
        Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth()*2, mCurrentPage.getHeight()*2,
                Bitmap.Config.ARGB_8888);
        mCurrentPage.render(bitmap, null, null, /*PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY*/PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
		mImageView.setBackgroundColor(Color.WHITE);
        mImageView.setImageBitmap(bitmap);
        updateUi();
    }

    
    private void closeRenderer() throws IOException {
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        mPdfRenderer.close();
        mFileDescriptor.close();
    }

    @Override
    protected void onDestroy() {
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /*@Override
    protected void onPause() {
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onPause();
    }*/

    
    private void updateUi() {
        int index = mCurrentPage.getIndex();
        int pageCount = mPdfRenderer.getPageCount();
        btnPrevious.setEnabled(0 != index);
        btnNext.setEnabled(index + 1 < pageCount);

    }
    public int getPageCount() {
        return mPdfRenderer.getPageCount();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_previous: {
                
                showPage(mCurrentPage.getIndex() - 1);
                break;
            }
            case R.id.btn_next: {
               
                showPage(mCurrentPage.getIndex() + 1);
                break;
            }
        }
    }
	private float x1 = 0;
	private float x2 = 0;
	private float y1 = 0;
	private float y2 = 0;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			x1 = event.getX();
			y1 = event.getY();
		}
		if(event.getAction() == MotionEvent.ACTION_UP) {
			x2 = event.getX();
			y2 = event.getY();
			/*if(y1 - y2 > 50) {
				Toast.makeText(PrizeCustomPdfSettingsActivity.this, "向上滑", Toast.LENGTH_SHORT).show();
			} else if(y2 - y1 > 50) {
				Toast.makeText(PrizeCustomPdfSettingsActivity.this, "向下滑", Toast.LENGTH_SHORT).show();
			} else */if(x1 - x2 > 20) {
				showPage(mCurrentPage.getIndex() + 1);
				//Toast.makeText(PrizeCustomPdfSettingsActivity.this, "向左滑", Toast.LENGTH_SHORT).show();
			} else if(x2 - x1 > 20) {
				showPage(mCurrentPage.getIndex() - 1<0?0:(mCurrentPage.getIndex() - 1));
				//Toast.makeText(PrizeCustomPdfSettingsActivity.this, "向右滑", Toast.LENGTH_SHORT).show();
			}
		}
		return super.onTouchEvent(event);
	}
	
}
