package com.example.daviswu.debttracker;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.net.Uri;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AllDebtFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AllDebtFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllDebtFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View rootView;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Item> mList;
    private int editLocation;
    private CardAdapter cardAdapter;
    private TextView debtFree;
    private int lastPosition = -1;
    private RecyclerView recyclerView;
    private String dir;
    private FloatingActionButton newItemButton;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllDebtFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllDebtFragment newInstance(String param1, String param2) {
        AllDebtFragment fragment = new AllDebtFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public AllDebtFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_all_debt, container, false);

        mList = new ArrayList<Item>();

        cardAdapter = new CardAdapter(mList);
        dir = Environment.getExternalStorageDirectory().toString()+File.separator+"DebtTracker";
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(cardAdapter);

        initializeSaves();
        debtFree = (TextView) rootView.findViewById(R.id.debt_free_text);
        if (mList.size() > 0) {
            debtFree.setVisibility(View.INVISIBLE);
        }

        newItemButton = (FloatingActionButton) rootView.findViewById(R.id.fab);
        newItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item itemSent = new Item();
                Intent intent = new Intent(getActivity(), NewDebtForm.class);
                intent.putExtra("myitem", itemSent);
                startActivityForResult(intent, 1);
            }
        });

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    private synchronized void stopThread(Thread theThread) {
        if (theThread != null) {
            theThread = null;
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initializeSaves() {
        // Deletes all files
        File directory = new File(dir+File.separator+"items/");
        if (directory.isDirectory()) {
            String[] children = directory.list();
            for (int i = 0; i < children.length; i++) {
                File item = new File(directory, children[i]);

                // Retrieve ret string
                String ret = "";
                String content = "";
                StringBuilder stringBuilder = new StringBuilder();
                try {
                    BufferedReader buf = new BufferedReader(new FileReader(item));
                    while ( (content = buf.readLine()) != null ) {
                        stringBuilder.append(content).append("\n");
                    }
                    buf.close();
                    ret = stringBuilder.toString();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Input into an item
                Item card = new Item();
                int count = 0;
                String path = "";
                for (int j = 0; j < ret.length(); j++) {
                    if (ret.charAt(j) == '|') {
                        count++;
                    }
                    else {
                        switch (count) {
                            case 0:
                                card.itemOwed += ret.charAt(j);
                                break;
                            case 1:
                                card.status += ret.charAt(j);
                                break;
                            case 2:
                                card.name += ret.charAt(j);
                                break;
                            case 3:
                                card.email += ret.charAt(j);
                                break;
                            case 4:
                                card.phone += ret.charAt(j);
                                break;
                            case 5:
                                path += ret.charAt(j);
                                break;
                            case 6:
                                break;
                        }
                    }
                }
                card.imgPath = new File(path);
                mList.add(card);
                cardAdapter.notifyDataSetChanged();
            }
        }
    }

    public void addItem(Item item) {
        mList.add(0, item);
        cardAdapter.notifyDataSetChanged();
        if (mList.size() > 0) {
            debtFree.setVisibility(View.INVISIBLE);
        }
        save();
    }

    public void deleteItem(int location) {
        Item item = mList.get(location);
        File file = new File(item.imgPath.getPath());
        file.delete();
        mList.remove(location);
        cardAdapter.notifyItemRemoved(location);
        if (mList.size() == 0) {
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(1000);
            debtFree.startAnimation(anim);
            debtFree.setVisibility(View.VISIBLE);
        }
        save();
    }

    public void editItem(int location, ImageView imageView) {
        Item item = mList.get(location);
        item.fileNumber = location;

        Intent intent = new Intent(getActivity(), EditViewActivity.class);
        intent.putExtra("awesomeitem", item);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), imageView, "image_transition");
        startActivityForResult(intent, 10002, options.toBundle());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle b = data.getExtras();
                if (b != null) {
                    Item myItem = (Item) b.getSerializable("Obj");
                    addItem(myItem);
                }
            } else if (resultCode == 0) {
                System.out.println("RESULT CANCELLED");
            }
        }
        else if (requestCode == 10002) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle d = data.getExtras();
                if (d != null) {
                    Item returnCard = (Item) d.getSerializable("Obj1");
                    mList.set(returnCard.fileNumber, returnCard);
                    cardAdapter.notifyItemChanged(returnCard.fileNumber);
                    save();
                }
            } else if (resultCode == 0) {
                System.out.println("RESULT CANCELLED");
            }
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void save() {
        // Deletes all files
        File directory = new File(dir+File.separator+"items/");
        if (directory.isDirectory()) {
            String[] children = directory.list();
            for (int i = 0; i < children.length; i++) {
                new File(directory, children[i]).delete();
            }
        }

        // Writes all files
        for (int i = 0; i<mList.size(); i++) {
            String filename = i+".txt";
            String content = mList.get(i).itemOwed+"|"
                    +mList.get(i).status+"|"
                    +mList.get(i).name+"|"
                    +mList.get(i).email+"|"
                    +mList.get(i).phone+"|";
            if (mList.get(i).imgPath.getAbsolutePath().length()>5) {
                content += mList.get(i).imgPath.getAbsolutePath()+"|";
            }
            else {
                content += "|";
            }

            try {
                File file = new File(dir+File.separator+"items/");
                file.mkdirs();
                FileOutputStream  fos = new FileOutputStream(file+File.separator+filename);
                OutputStreamWriter outWriter = new OutputStreamWriter(fos);
                outWriter.append(content);
                outWriter.close();
                fos.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }
}
