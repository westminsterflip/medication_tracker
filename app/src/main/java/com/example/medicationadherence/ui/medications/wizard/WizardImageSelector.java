package com.example.medicationadherence.ui.medications.wizard;


import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.FixedPreloadSizeProvider;
import com.example.medicationadherence.R;
import com.example.medicationadherence.adapter.ImageSelectorAdapter;
import com.example.medicationadherence.data.room.entities.MedData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class WizardImageSelector extends Fragment implements RootWizardFragment.ErrFragment, ImageSelectorAdapter.ImageClickListener {
    private RootWizardViewModel model;
    private int page = 1;
    private ArrayList<String> images = null;
    private ImageSelectorAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(Objects.requireNonNull(Objects.requireNonNull(getParentFragment()).getParentFragment())).get(RootWizardViewModel.class);
        if (model.getThisList().size() == 1)
            model.getThisList().add(this);
        else if (model.getThisList().get(1) != this)
            model.getThisList().set(1, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wizard_image_selector, container, false);
        final RecyclerView imageList = root.findViewById(R.id.imageRecyclerView);
        imageList.setLayoutManager(new GridLayoutManager(getContext(), 3));
        imageList.setHasFixedSize(true);
        try {
            images = new MedImageTask(model, page++).execute().get();
            if (images == null)
                images = new ArrayList<>();
            images.add(0, null);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        ListPreloader.PreloadSizeProvider sizeProvider = new FixedPreloadSizeProvider(1024,1024);
        MedImagePreloadProvider provider = new MedImagePreloadProvider();
        // noinspection unchecked
        RecyclerViewPreloader<ContactsContract.CommonDataKinds.Photo> preloader = new RecyclerViewPreloader<ContactsContract.CommonDataKinds.Photo>(Glide.with(this), provider, sizeProvider, 10);
        imageList.addOnScrollListener(preloader);
        if (images != null) {
            imageList.setAdapter(adapter = new ImageSelectorAdapter(images, model, model.getMedImage() == null));
        }
        imageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!imageList.canScrollVertically(1)) {
                    ArrayList<String> temp = null;
                    try {
                        temp = new MedImageTask(model, page++).execute().get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(temp != null){
                        for(String s : temp)
                            if (!images.contains(s))
                                images.add(s);
                        if(adapter != null)
                            adapter.notifyDataSetChanged();
                    } else {
                        imageList.setOnScrollChangeListener(null);
                    }
                }
            }
        });
        adapter.setListener(this);
        return root;
    }

    @Override
    public void showErrors() {

    }

    @Override
    public void prepareBack() {

    }

    @Override
    public void pause() {

    }

    @Override
    public boolean isExitable() {
        return true;
    }

    @Override
    public void onImageClick(int position) {
        adapter.setSelected(position);
        model.setSelPos(position);
    }

    //TODO: filter images
    private static class MedImageTask extends AsyncTask<Void, Void, ArrayList<String>> {
        RootWizardViewModel model;
        int page;

        MedImageTask(RootWizardViewModel model, int page) {
            this.model = model;
            this.page = page;
        }

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            try {
                ArrayList<String> out = new ArrayList<>();
                if (model.getMedImage() != null) {
                    out.add(model.getMedImage());
                    model.setSelPos(1);
                }
                URL medAPI;
                HttpsURLConnection apiConn;
                if (page == 1) {
                    ArrayList<Integer> idList = new ArrayList<>();
                    for (MedData i : model.getDataList()) {
                        if (i.getDosage().equals(model.getMedDosage()) && i.getName().equals(model.getMedName())) {
                            idList.add(i.getId());
                            if (i.getAltId() != -1)
                                idList.add(i.getAltId());
                        }
                    }
                    for (int id : idList) {
                        medAPI = new URL("https://rximage.nlm.nih.gov/api/rximage/1/rxbase?rxcui=" + id + "&resolution=1024");
                        apiConn = (HttpsURLConnection) medAPI.openConnection();
                        if (apiConn.getResponseCode() == 200) {
                            InputStream response = apiConn.getInputStream();
                            InputStreamReader responseReader = new InputStreamReader(response, StandardCharsets.UTF_8);
                            JsonReader jsonReader = new JsonReader(responseReader);
                            JsonToken token = jsonReader.peek();
                            while (token != JsonToken.END_DOCUMENT) {
                                switch (token) {
                                    case BEGIN_OBJECT:
                                        jsonReader.beginObject();
                                        break;
                                    case BEGIN_ARRAY:
                                        jsonReader.beginArray();
                                        break;
                                    case BOOLEAN:
                                        jsonReader.nextBoolean();
                                        break;
                                    case END_ARRAY:
                                        jsonReader.endArray();
                                        break;
                                    case END_OBJECT:
                                        jsonReader.endObject();
                                        break;
                                    case NAME:
                                        if (jsonReader.nextName().equals("imageUrl")) {
                                            String url = jsonReader.nextString();
                                            if (!out.contains(url))
                                                out.add(url);
                                        }
                                        break;
                                    case NULL:
                                        jsonReader.nextNull();
                                        break;
                                    case NUMBER:
                                        jsonReader.nextDouble();
                                        break;
                                    case STRING:
                                        jsonReader.nextString();
                                        break;
                                    default:
                                        //unknown symbol
                                }
                                token = jsonReader.peek();
                            }
                            jsonReader.close();
                            apiConn.disconnect();
                        }
                    }
                }
                medAPI = new URL("https://rximage.nlm.nih.gov/api/rximage/1/rxbase?name=" + model.getMedName().replaceAll("\\s*\\([^\\)]*\\)\\s*","") + "&rPage=" + page + "&resolution=1024");
                apiConn = (HttpsURLConnection) medAPI.openConnection();
                if (apiConn.getResponseCode() == 200) {
                    InputStream response = apiConn.getInputStream();
                    InputStreamReader responseReader = new InputStreamReader(response, StandardCharsets.UTF_8);
                    JsonReader jsonReader = new JsonReader(responseReader);
                    JsonToken token = jsonReader.peek();
                    while (token != JsonToken.END_DOCUMENT) {
                        switch (token) {
                            case BEGIN_OBJECT:
                                jsonReader.beginObject();
                                break;
                            case BEGIN_ARRAY:
                                jsonReader.beginArray();
                                break;
                            case BOOLEAN:
                                jsonReader.nextBoolean();
                                break;
                            case END_ARRAY:
                                jsonReader.endArray();
                                break;
                            case END_OBJECT:
                                jsonReader.endObject();
                                break;
                            case NAME:
                                if (jsonReader.nextName().equals("imageUrl")) {
                                    String url = jsonReader.nextString();
                                    if (!out.contains(url))
                                        out.add(url);
                                }
                                break;
                            case NULL:
                                jsonReader.nextNull();
                                break;
                            case NUMBER:
                                jsonReader.nextDouble();
                                break;
                            case STRING:
                                jsonReader.nextString();
                                break;
                            default:
                                //unknown symbol
                        }
                        token = jsonReader.peek();
                    }
                    jsonReader.close();
                    apiConn.disconnect();
                    return out;
                } else {
                    try {
                        throw new Exception("API connection failed");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class MedImagePreloadProvider implements ListPreloader.PreloadModelProvider{
        @NonNull
        @Override
        public List getPreloadItems(int position) {
            String url = images.get(position);
            if (TextUtils.isEmpty(url))
                return Collections.emptyList();
            return Collections.singletonList(url);
        }

        @Nullable
        @Override
        public RequestBuilder<?> getPreloadRequestBuilder(@NonNull Object item) {
            return Glide.with(Objects.requireNonNull(getContext())).load((String) item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        model.setMedImage(images.get(model.getSelPos()));
    }
}
