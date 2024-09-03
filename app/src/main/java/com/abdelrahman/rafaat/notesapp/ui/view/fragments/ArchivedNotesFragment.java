package com.abdelrahman.rafaat.notesapp.ui.view.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abdelrahman.rafaat.notesapp.R;
import com.abdelrahman.rafaat.notesapp.databinding.FragmentHomeBinding;
import com.abdelrahman.rafaat.notesapp.model.Note;
import com.abdelrahman.rafaat.notesapp.ui.view.NotesAdapter;
import com.abdelrahman.rafaat.notesapp.ui.view.OnNotesClickListener;
import com.abdelrahman.rafaat.notesapp.ui.viewmodel.NoteViewModel;

import java.util.ArrayList;
import java.util.List;

public class ArchivedNotesFragment extends BaseFragment implements OnNotesClickListener {

    private FragmentHomeBinding binding;
    private NoteViewModel noteViewModel;
    private NotesAdapter adapter;
    private List<Note> archivedNotes = new ArrayList<>();
    private Note selectedNote;
    private AlertDialog alertDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.addNoteFloatingActionButton.setVisibility(View.GONE);
        binding.noteSearchView.setVisibility(View.GONE);
        initRecyclerView();
        initViewModel();
        observeViewModel();
        onBackPressed();
    }

    private void initRecyclerView() {
        adapter = new NotesAdapter(this);
        binding.notesRecyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.notesRecyclerview.setAdapter(adapter);
        int resId = R.anim.lat;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        binding.notesRecyclerview.setLayoutAnimation(animation);
        swipeRecyclerView();
    }

    private void swipeRecyclerView() {
        MyItemTouchHelperCallback simpleCallback = new MyItemTouchHelperCallback(requireContext(), binding.notesRecyclerview) {
            @Override
            public void deleteButtonPressed(int position) {
                selectedNote = archivedNotes.get(position);
                showAlertDialog(position);
            }

            @Override
            public void archiveButtonPressed(int position) {
                selectedNote = archivedNotes.get(position);
                selectedNote.setArchived(!selectedNote.isArchived());
                noteViewModel.updateNote(selectedNote);
            }

            @Override
            public void pinButtonPressed(int position) {
                selectedNote = archivedNotes.get(position);
                selectedNote.setPinned(!selectedNote.isPinned());
                noteViewModel.updateNote(selectedNote);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.notesRecyclerview);
    }

    private void showAlertDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialog);

        builder.setMessage(getString(R.string.remove_note))
                .setPositiveButton(R.string.remove, (dialog, which) -> noteViewModel.deleteNote(selectedNote.getId()))
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    alertDialog.dismiss();
                    adapter.notifyItemChanged(position);
                });

        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void initViewModel() {
        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);
        noteViewModel.getArchivedNotes();
    }

    private void observeViewModel() {
        noteViewModel.archivedNotes.observe(getViewLifecycleOwner(), notes -> {
            archivedNotes = notes;
            if (notes.isEmpty()) {
                binding.noNotesLayout.noNotesView.setVisibility(View.VISIBLE);
            } else {
                binding.noNotesLayout.noNotesView.setVisibility(View.GONE);
            }

            binding.noSearchLayout.noFilesView.setVisibility(View.GONE);
            adapter.setList(notes);
        });
    }

    public void onBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setEnabled(false);
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    @Override
    public void onNoteClickListener(Note note) {

    }
}
