package com.focusforge.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WebsitesFragment extends Fragment {
    private RecyclerView recyclerView;
    private WebsiteAdapter adapter;
    private EditText editDomain;
    private List<String> domains;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_websites, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerWebsites);
        editDomain = view.findViewById(R.id.editDomain);
        Button btnAdd = view.findViewById(R.id.btnAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        domains = new ArrayList<>(FocusForgeConfig.blockedDomains);
        adapter = new WebsiteAdapter();
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            String domain = editDomain.getText().toString().trim().toLowerCase();
            if (domain.isEmpty()) {
                Toast.makeText(getActivity(), "Enter a domain", Toast.LENGTH_SHORT).show();
                return;
            }
            if (domain.startsWith("http://")) domain = domain.substring(7);
            if (domain.startsWith("https://")) domain = domain.substring(8);
            if (domain.startsWith("www.")) domain = domain.substring(4);
            if (domain.contains("/")) domain = domain.substring(0, domain.indexOf("/"));

            if (FocusForgeConfig.blockedDomains.contains(domain)) {
                Toast.makeText(getActivity(), "Already blocked", Toast.LENGTH_SHORT).show();
                return;
            }

            FocusForgeConfig.blockedDomains.add(domain);
            domains.add(domain);
            adapter.notifyItemInserted(domains.size() - 1);
            editDomain.setText("");
            Toast.makeText(getActivity(), "Blocked: " + domain, Toast.LENGTH_SHORT).show();
        });
    }

    private class WebsiteAdapter extends RecyclerView.Adapter<WebsiteAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_website, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String domain = domains.get(position);
            holder.websiteDomain.setText(domain);
            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                String removed = domains.remove(pos);
                FocusForgeConfig.blockedDomains.remove(removed);
                notifyItemRemoved(pos);
            });
        }

        @Override
        public int getItemCount() {
            return domains.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView websiteDomain;
            View btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                websiteDomain = itemView.findViewById(R.id.websiteDomain);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }
}
