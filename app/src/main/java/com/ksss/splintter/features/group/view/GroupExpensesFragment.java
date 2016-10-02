package com.ksss.splintter.features.group.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.ksss.splintter.R;
import com.ksss.splintter.features.group.backend.PersonBO;
import com.ksss.splintter.features.group.backend.PersonBOImpl;
import com.ksss.splintter.features.group.backend.exception.EmptyNameException;
import com.ksss.splintter.features.group.backend.exception.NameTooShortException;
import com.ksss.splintter.features.group.domain.Expense;
import com.ksss.splintter.features.group.domain.Group;
import com.ksss.splintter.features.group.domain.Member;
import hugo.weaving.DebugLog;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import timber.log.Timber;

/**
 * Created by Nahuel Barrios on 7/16/16.
 */
public class GroupExpensesFragment extends Fragment {

    private Group group;

    private AutoCompleteTextView personEditText;
    private AutoCompleteTextView descriptionEditText;
    private EditText amountEditText;
    private RecyclerView expensesRecyclerView;

    private PersonBO personBO;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        personBO = new PersonBOImpl();

        group = getCallback().getGroup();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View layout = inflater.inflate(R.layout.group_expenses_layout, container, false);
        personEditText = (AutoCompleteTextView) layout.findViewById(R.id.add_expense_person);
        amountEditText = (EditText) layout.findViewById(R.id.add_expense_amount);
        descriptionEditText = (AutoCompleteTextView) layout.findViewById(R.id.add_expense_description);
        expensesRecyclerView = (RecyclerView) layout.findViewById(R.id.group_view_expenses_recycler_view);

        setupRecyclerView();

        IconDrawable iconDrawable = new IconDrawable(getActivity(), MaterialIcons.md_plus_one);
        iconDrawable.colorRes(android.R.color.white);

        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.fab);
        fab.setImageDrawable(iconDrawable);
        fab.setOnClickListener(new View.OnClickListener() {

            @DebugLog
            @Override
            public void onClick(View view) {
                // TODO: 7/18/16 Add person?
                Timber.e("What should I do in this case???");
                onNewExpenseAdded();

            }
        });

        return layout;
    }

    private void setupRecyclerView() {
        expensesRecyclerView.setHasFixedSize(true);

        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        expensesRecyclerView.setAdapter(new ExpensesAdapter(getGroupExpenses()));
    }

    private List<Expense> getGroupExpenses() {

        List<Expense> result = new ArrayList<>();

        for (Member eachMember : group.getMembers()) {
            for (Expense eachExpense : eachMember.getExpenses()) {
                result.add(eachExpense);
            }
        }

        return result;
    }

    @Override
    public void onResume() {
        super.onResume();

        setupMembersAutocomplete();
        setupDescriptionAutocomplete();
    }

    private void setupMembersAutocomplete() {
        personEditText.setAdapter(new MembersAdapter(getContext(), getCallback().getGroup().getMembers()));
    }

    private void setupDescriptionAutocomplete() {
        // TODO: 7/17/16 Make it dynamic! realm.io comes to playground again =)
        String[] hardCodedSuggestions = {"Bebida", "Nafta", "Pizzas"};

        // TODO: 7/17/16 I don't need an adapter like MembersAdapter here because descriptions are just a simple String
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, hardCodedSuggestions);
        descriptionEditText.setAdapter(adapter);
    }

    private void onNewExpenseAdded() {
        Member member = ((MembersAdapter) personEditText.getAdapter()).findByName(personEditText.getText().toString());

        group = getCallback().getGroup();
        if (member == null) {
            // TODO: 7/17/16 Make a class diagram taking into account that members will share groups so we must know which expense is related to which group
            try {
                personBO.create(personEditText.getText().toString());
                // TODO: 7/19/16 Update person adapter!
            } catch (EmptyNameException e) {
                e.printStackTrace();
            } catch (NameTooShortException e) {
                e.printStackTrace();
            }

            member = new Member(personEditText.getText().toString());
            // TODO: 7/19/16 Replace this list#for with a call to the service
            group.addMember(member);
        }

        member.addExpense(new BigDecimal(amountEditText.getText().toString()), descriptionEditText.getText().toString());
        // TODO: 7/17/16 What about Realm.io?
    }

    /**
     * // TODO: 7/17/16 I can't stand with this
     *
     * @return
     */
    private ExpenseManager getCallback() {
        ExpenseManager callback;
        try {
            callback = ((ExpenseManager) getActivity());
        } catch (ClassCastException e) {
            throw new IllegalStateException(
                String.format("Container Activity must implement %s", ExpenseManager.class.getSimpleName()), e);
            // TODO: 7/17/16 Add log?
        }

        return callback;
    }

    private class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ViewHolder> {

        private final List<Expense> expenses;

        public ExpensesAdapter(@NonNull List<Expense> groupExpenses) {
            expenses = groupExpenses;
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView person;
            private final TextView amount;
            private final TextView description;
            private final TextView date;

            public ViewHolder(View layout) {
                super(layout);
                person = (TextView) layout.findViewById(R.id.expense_person);
                amount = (TextView) layout.findViewById(R.id.expense_amount);
                description = (TextView) layout.findViewById(R.id.expense_description);
                date = (TextView) layout.findViewById(R.id.expense_date);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getActivity().getLayoutInflater().inflate(R.layout.group_view_expense, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Expense expense = expenses.get(position);
            holder.person.setText("Nahue");
            holder.amount.setText(expense.getAmount().toString());
            holder.description.setText(expense.getDescription());
            holder.date.setText(new SimpleDateFormat("MMMM, dd", Locale.getDefault()).format(expense.getDate().getTime()));
        }

        @Override
        public int getItemCount() {
            return expenses.size();
        }

        @Override
        public String toString() {
            return "ExpensesAdapter{" +
                "expenses=" + expenses +
                '}';
        }
    }
}
