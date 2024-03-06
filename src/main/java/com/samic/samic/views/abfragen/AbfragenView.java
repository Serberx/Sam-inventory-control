package com.samic.samic.views.abfragen;

import com.samic.samic.components.UIFactory;
import com.samic.samic.data.entity.Type;
import com.samic.samic.views.MainLayout;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;

@PageTitle("Abfragen")
@Route(value = "abfragen", layout = MainLayout.class)
@RolesAllowed({"MANAGMENT", "STORAGEADMIN"})
public class AbfragenView extends VerticalLayout {

  private static final List<String> MONTH_NAMES = List.of("Januar", "Februar", "März", "April",
      "Mai",
      "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember");
  private static final List<String> WEEK_DAYS_NAMES = List.of
      ("Sonntag", "Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag");
  private static final List<String> WEEK_DAYS_NAMES_SHORT = List.of("So", "Mo", "Di", "Mi", "Do",
      "Fr", "Sa");
  private final DatePicker dateFrom = new DatePicker("Von");
  private final DatePicker dateTo = new DatePicker("Bis");
  private final RadioButtonGroup<String> inOut = new RadioButtonGroup<>("Flussrichtung");
  private final ComboBox<Type> deviceType = new ComboBox<>("Gerätetyp");
  private final TextField usageAsProjectHardwareValue = new TextField();
  private final TextField usagAsStandardHardware = new TextField();
  private final TextField amountPrepared = new TextField();
  private final TextField amountNew = new TextField();
  private final Binder<StatObject> statObjectBinder = new Binder<>(StatObject.class);
  private final DataProviderAbfragen dataProviderAbfragen;

  public AbfragenView(DataProviderAbfragen dataProviderAbfragen) {
    this.dataProviderAbfragen = dataProviderAbfragen;

    initBinder();
    initUI();
    initActions();
  }


  private void initUI() {
    usagAsStandardHardware.isReadOnly();
    usageAsProjectHardwareValue.isReadOnly();
    amountNew.isReadOnly();
    amountPrepared.isReadOnly();

    usageAsProjectHardwareValue.setMaxWidth("100px");
    usagAsStandardHardware.setMaxWidth("100px");
    amountNew.setMaxWidth("100px");
    amountPrepared.setMaxWidth("100px");

    deviceType.setItemLabelGenerator(Type::getLongVersion);
    deviceType.setItems(Type.values());

    DatePicker.DatePickerI18n germanI18n = new DatePickerI18n();

    germanI18n.setMonthNames(MONTH_NAMES);
    germanI18n.setWeekdays(WEEK_DAYS_NAMES);
    germanI18n.setWeekdaysShort(WEEK_DAYS_NAMES_SHORT);
    germanI18n.setFirstDayOfWeek(1);
    germanI18n.setDateFormat("dd.MM.yyyy");

    dateTo.setI18n(germanI18n);
    dateFrom.setI18n(germanI18n);

    inOut.setItems("Eingehend", "Ausgehend");
    add(UIFactory.rootComponentContainer("",
            UIFactory.childContainer(JustifyContentMode.START,
                dateFrom,
                dateTo,
                inOut,
                deviceType
            )),
        UIFactory.rootComponentContainer("",
            UIFactory.childContainer(JustifyContentMode.BETWEEN,
                new FormLayout(new Text("Verbr. als Projekthardware"), usageAsProjectHardwareValue),
                new FormLayout(new Text("Verbr. als Standard Hardware"), usagAsStandardHardware),
                new FormLayout(new Text("Anz. Aufbereitet"), amountPrepared),
                new FormLayout(new Text("Anz. Neu"), amountNew)
            )
        ));

    //Display dummy Object
    statObjectBinder.setBean(dataProviderAbfragen.getStatObject());
  }

  private void initActions() {
    dateFrom.addValueChangeListener(e -> dateFrom.setMin(e.getValue()));
    dateTo.addValueChangeListener(e -> dateTo.setMin(e.getValue()));

  }

  private void initBinder() {
    statObjectBinder.forField(usagAsStandardHardware)
        .withConverter(new StringToIntegerConverter(""))
        .bind(StatObject::getAmountStandardHardware, null);
    statObjectBinder.forField(usageAsProjectHardwareValue)
        .withConverter(new StringToIntegerConverter(""))
        .bind(StatObject::getAmountProjectHardware, null);
    statObjectBinder.forField(amountNew)
        .withConverter(new StringToIntegerConverter(""))
        .bind(StatObject::getAmountnewHardware, null);
    statObjectBinder.forField(amountPrepared)
        .withConverter(new StringToIntegerConverter(""))
        .bind(StatObject::getAmountPrepared, null);
  }
}
