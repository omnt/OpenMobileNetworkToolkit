/*
 * SPDX-FileCopyrightText:  2025 Peter Hasse <peter.hasse@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Johann Hackler <johann.hackler@fokus.fraunhofer.de>
 * SPDX-FileCopyrightText: 2025 Fraunhofer FOKUS
 *
 *  SPDX-License-Identifier: BSD-3-Clause-Clear
 */

package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ScannerFragment extends Fragment {

    private PreviewView previewView;
    private Executor cameraExecutor;
    private MultiFormatReader barcodeReader;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);
        previewView = view.findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();
        setupBarcodeReader();


        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d("ScannerFragment", "Camera permission granted. Starting camera...");
                        startCamera(); // call your camera setup here
                    } else {
                        Log.e("ScannerFragment", "Camera permission denied.");
                        Toast.makeText(getContext(), "Camera permission is required to scan QR codes.", Toast.LENGTH_LONG).show();
                    }
        });


        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        return view;
    }

    private void setupBarcodeReader() {
        barcodeReader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS,
                Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.EAN_13, BarcodeFormat.CODE_128));
        barcodeReader.setHints(hints);
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    if (image.getFormat() == ImageFormat.YUV_420_888) {
                        @SuppressLint("UnsafeOptInUsageError")
                        Image mediaImage = image.getImage();
                        if (mediaImage != null) {
                            int width = mediaImage.getWidth();
                            int height = mediaImage.getHeight();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] data = new byte[buffer.remaining()];
                            buffer.get(data);

                            PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                                    data, width, height,
                                    0, 0, width, height,
                                    false
                            );

                            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                            try {
                                Result result = barcodeReader.decode(bitmap);
                                Log.d("ZXing", "Scanned: " + result.getText());

                                requireActivity().runOnUiThread(() -> {
                                    Bundle bundleResult = new Bundle();
                                    bundleResult.putString("scanned_qr", result.getText());
                                    getParentFragmentManager().setFragmentResult("qr_scan_request", bundleResult);
                                    NavHostFragment.findNavController(this).popBackStack();
                                });

                            } catch (NotFoundException e) {
                                // No QR code found, ignore
                            } finally {
                                image.close();
                            }
                        } else {
                            image.close();
                        }
                    } else {
                        image.close();
                    }
                });


                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
