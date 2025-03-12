package com.example.rateyourplace;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class findAddress extends DialogFragment {

    private EditText searchBox;
    private ListView addressListView;
    private ArrayAdapter<String> adapter;
    private List<String> addressSuggestions = new ArrayList<>();
    private List<double[]> coordinatesList = new ArrayList<>();
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(String address, double latitude, double longitude);
    }

    public findAddress(OnAddressSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_find_address, null);
        builder.setView(view).setTitle("Find Address");

        searchBox = view.findViewById(R.id.searchBox);
        Button searchBtn = view.findViewById(R.id.searchBtn);
        addressListView = view.findViewById(R.id.addressSuggestionsList);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, addressSuggestions);
        addressListView.setAdapter(adapter);

        searchBtn.setOnClickListener(v -> {
            String query = searchBox.getText().toString().trim();
            if (!query.isEmpty()) {
                new FetchAddressesTask().execute(query);
            } else {
                Toast.makeText(getActivity(), "Enter an address", Toast.LENGTH_SHORT).show();
            }
        });

        addressListView.setOnItemClickListener((AdapterView<?> parent, View view1, int position, long id) -> {
            String selectedAddress = addressSuggestions.get(position);
            double[] coordinates = coordinatesList.get(position);

            if (listener != null) {
                listener.onAddressSelected(selectedAddress, coordinates[0], coordinates[1]);
            }
            dismiss();
        });

        return builder.create();
    }

    private class FetchAddressesTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... params) {
            String query = params[0];
            List<String> results = new ArrayList<>();
            coordinatesList.clear();

            try {
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                String urlString = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedQuery;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonResponse = new JSONArray(response.toString());

                for (int i = 0; i < jsonResponse.length(); i++) {
                    JSONObject place = jsonResponse.getJSONObject(i);
                    String displayName = place.getString("display_name");
                    double lat = place.getDouble("lat");
                    double lon = place.getDouble("lon");

                    results.add(displayName);
                    coordinatesList.add(new double[]{lat, lon});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            addressSuggestions.clear();
            addressSuggestions.addAll(result);
            adapter.notifyDataSetChanged();
            addressListView.setVisibility(View.VISIBLE);
        }
    }
}
