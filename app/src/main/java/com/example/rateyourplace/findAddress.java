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
    //Set global variables
    private EditText searchBox;
    private ListView addressListView;
    private ArrayAdapter<String> adapter;
    private List<String> addressSuggestions = new ArrayList<>();
    private List<double[]> coordinatesList = new ArrayList<>();
    private OnAddressSelectedListener listener;

    public interface OnAddressSelectedListener {
        void onAddressSelected(String address, double latitude, double longitude);
    }

    //Action listener
    public findAddress(OnAddressSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Creates dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_find_address, null);
        builder.setView(view).setTitle("Find Address");

        //Assign xml components to variables
        searchBox = view.findViewById(R.id.searchBox);
        Button searchBtn = view.findViewById(R.id.searchBtn);
        addressListView = view.findViewById(R.id.addressSuggestionsList);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, addressSuggestions);
        addressListView.setAdapter(adapter);

        //Action listener for submit button
        searchBtn.setOnClickListener(v -> {
            //Gets search query
            String query = searchBox.getText().toString().trim();
            if (!query.isEmpty()) {
                //If the user provides an address calls method to get details
                new FetchAddressesTask().execute(query);
            } else {
                Toast.makeText(getActivity(), "Enter an address", Toast.LENGTH_SHORT).show();
            }
        });

        //Gets details for the address selected
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

    //Finds addresses to display in list
    private class FetchAddressesTask extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... params) {
            //Extract the search query from parameters
            String query = params[0];
            List<String> results = new ArrayList<>();

            //Clear previous coordinate results to avoid duplicates
            coordinatesList.clear();

            try {
                //Encode the query to ensure it is properly formatted for a URL
                String encodedQuery = URLEncoder.encode(query, "UTF-8");
                // Construct the API URL using OpenStreetMap's Nominatim service
                String urlString = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedQuery;
                //Open an HTTP connection to the URL
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                //Read the response from the API
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                //Read each line from the response and append it to the response string
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Convert the response into a JSON array
                JSONArray jsonResponse = new JSONArray(response.toString());

                // Loop through each result and extract relevant details
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
            //Clear previous address suggestions to update with new results
            addressSuggestions.clear();
            //Add the newly retrieved addresses to the suggestions list
            addressSuggestions.addAll(result);
            //Notify the adapter that the data has changed so the UI updates
            adapter.notifyDataSetChanged();
            //Make the address list visible to the user
            addressListView.setVisibility(View.VISIBLE);
        }
    }
}
