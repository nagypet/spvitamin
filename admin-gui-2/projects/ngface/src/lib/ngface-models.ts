/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2026-01-29 07:09:51.

export namespace Ngface {

    export interface DataRetrievalParams {
        page: DataRetrievalParams.Page | null;
        sort: DataRetrievalParams.Sort | null;
        filters: DataRetrievalParams.Filter[] | null;
    }

    export interface Menu {
        items: Menu.Item[];
        defaultItemId: string;
    }

    export interface RowSelectParams<T> extends Serializable {
        selectMode: RowSelectParams.SelectMode;
        rows: RowSelectParams.Row<T>[];
    }

    export interface SubmitFormData {
        id: string;
        widgetDataMap: { [index: string]: WidgetData };
    }

    export interface TableActionParams<T> {
        actionTriggerMode: ActionTriggerMode;
        actionId: string;
        rowId: T | null;
    }

    export interface Button extends Widget<VoidWidgetData, Button> {
        type: "Button";
        data: VoidWidgetData;
        style: Button.Style;
        badge: string;
    }

    export interface WidgetList extends Widget<WidgetList.Data, WidgetList> {
        type: "WidgetList";
        data: WidgetList.Data;
        widgets: { [index: string]: Widget<any, any> };
    }

    export namespace WidgetList {

        export interface Data extends WidgetData {
            type: "WidgetList.Data";
            widgetDataMap: { [index: string]: WidgetData };
        }

    }

    export interface Form {
        id: string;
        title: string;
        widgets: { [index: string]: Widget<any, any> };
    }

    export interface FormattedText extends Widget<FormattedText.Data, FormattedText> {
        type: "FormattedText";
        data: FormattedText.Data;
    }

    export namespace FormattedText {

        export interface Data extends Value<string> {
            type: "FormattedText.Data";
            value: string | null;
        }

    }

    export interface AbstractOption extends Comparable<AbstractOption> {
        texts: string[];
        id: string;
        type: string;
    }

    export interface Autocomplete extends Input<Autocomplete.Data, string, Autocomplete> {
        type: "Autocomplete";
        data: Autocomplete.Data;
    }

    export namespace Autocomplete {

        export interface Data extends Value<string> {
            type: "Autocomplete.Data";
            value: string | null;
            extendedReadOnlyData: Autocomplete.Data.ExtendedReadOnlyData;
        }

    }

    export namespace Autocomplete.Data {

        export interface ExtendedReadOnlyData {
            valueSet: ValueSet;
        }

    }

    export interface AutocompleteOption extends AbstractOption {
    }

    export interface DateInput extends Input<DateInput.Data, Date, DateInput> {
        type: "DateInput";
        data: DateInput.Data;
    }

    export namespace DateInput {

        export interface Data extends Value<Date> {
            type: "DateInput.Data";
            value: Date | null;
        }

    }

    export interface DateRangeInput extends Input<DateRangeInput.Data, void, DateRangeInput> {
        type: "DateRangeInput";
        data: DateRangeInput.Data;
        placeholder2: string;
        validators2: Validator[];
    }

    export namespace DateRangeInput {

        export interface Data extends WidgetData {
            type: "DateRangeInput.Data";
            startDate: Date;
            endDate: Date;
        }

    }

    export interface DateTimeInput extends Input<DateTimeInput.Data, Date, DateTimeInput> {
        type: "DateTimeInput";
        data: DateTimeInput.Data;
    }

    export namespace DateTimeInput {

        export interface Data extends Value<Date> {
            type: "DateTimeInput.Data";
            value: Date | null;
        }

    }

    export interface GenericAutocomplete<T> extends Input<GenericAutocomplete.Data<T>, T, GenericAutocomplete<T>> {
        type: "GenericAutocomplete";
        data: GenericAutocomplete.Data<T>;
    }

    export namespace GenericAutocomplete {

        export interface Data<T> extends Value<T> {
            type: "GenericAutocomplete.Data";
            value: T | null;
            extendedReadOnlyData: GenericAutocomplete.Data.ExtendedReadOnlyData<T>;
        }

    }

    export namespace GenericAutocomplete.Data {

        export interface ExtendedReadOnlyData<T> {
            valueSet: GenericValueSet<T>;
        }

    }

    export interface GenericValueSet<T> extends Serializable {
        remote: boolean;
        truncated: boolean;
        values: GenericValueSet.Item<T>[];
    }

    export namespace GenericValueSet {

        export interface Item<T> extends Serializable {
            value: T;
            selected: boolean;
        }

    }

    export interface NumericInput extends Input<NumericInput.Data, number, NumericInput> {
        type: "NumericInput";
        data: NumericInput.Data;
        format: NumericFormat;
    }

    export namespace NumericInput {

        export interface Data extends Value<number> {
            type: "NumericInput.Data";
            value: number | null;
        }

    }

    export interface Select extends Input<Select.Data, void, Select> {
        type: "Select";
        data: Select.Data;
    }

    export namespace Select {

        export interface Data extends WidgetData {
            type: "Select.Data";
            options: { [index: string]: string };
            selected: string;
        }

    }

    export interface Option {
        id: string;
        value: string;
    }

    export interface TextInput extends Input<TextInput.Data, string, TextInput> {
        type: "TextInput";
        data: TextInput.Data;
        password: boolean | null;
    }

    export namespace TextInput {

        export interface Data extends Value<string> {
            type: "TextInput.Data";
            value: string | null;
        }

    }

    export interface Email extends Validator {
        type: "Email";
    }

    export interface Max extends Validator {
        type: "Max";
        max: number;
    }

    export interface Min extends Validator {
        type: "Min";
        min: number;
    }

    export interface Pattern extends Validator {
        type: "Pattern";
        pattern: string;
    }

    export interface Required extends Validator {
        type: "Required";
    }

    export interface Size extends Validator {
        type: "Size";
        min: number;
        max: number;
    }

    export interface Action {
        id: string;
        label: string;
        text: string;
        textPlacement: TextPlacement;
        icon: string;
        enabled: boolean;
        badge: string;
        style: Action.Style;
        actions: Action[] | null;
    }

    export interface Column {
        id: string;
        text: string;
        sortable: boolean;
        size: Column.Size;
        textAlign: Column.TextAlign;
    }

    export interface ColumnGroup {
        id: string;
        text: string;
        colSpan: number;
        textAlign: ColumnGroup.TextAlign;
        valid: boolean;
    }

    export interface Filterer extends Serializable {
        column: string;
        operator: ComparisonOperator;
        valueSet: ValueSet;
        searchText: string;
        active: boolean;
        type: Type;
        order: number;
    }

    export interface FiltererFactory {
        filtererDefMap: { [index: string]: FiltererDef };
    }

    export interface FiltererDef {
        column: string;
        remote: boolean;
        valueProvider: ValueProvider<string, string[]>;
        type: Type;
        order: number;
    }

    export interface Paginator extends Serializable {
        pageIndex: number;
        pageSize: number;
        length: number;
        pageSizeOptions: number[];
    }

    export interface ParameterizedValueProvider<T, R> extends ValueProvider<T, R> {
        parameter: string;
        function: BiFunction<string, T, R>;
    }

    export interface Row<T> {
        id: T;
        idType: string;
        cells: { [index: string]: Cell<any, any> };
        additionalInfo: { [index: string]: any };
        selected: boolean;
        disabled: boolean;
    }

    export interface Sorter extends Serializable {
        column: string;
        direction: Direction;
    }

    export interface Table<T> extends Widget<Table.Data, Table<T>> {
        type: "Table";
        data: Table.Data;
        columnGroups: { [index: string]: ColumnGroup };
        columns: { [index: string]: Column };
        rows: Row<T>[];
        totalRow: Row<T> | null;
        selectMode: Table.SelectMode;
        notification: string | null;
        countSelectedRows: number;
    }

    export namespace Table {

        export interface Data extends WidgetData, Serializable {
            type: "Table.Data";
            paginator: Paginator | null;
            sorter: Sorter | null;
            filtererMap: { [index: string]: Filterer };
        }

    }

    export interface TableDataBuilder {
    }

    export interface ValueProvider<T, R> {
    }

    export interface ValueSet extends Serializable {
        remote: boolean;
        truncated: boolean;
        values: ValueSet.Item[];
    }

    export namespace ValueSet {

        export interface Item extends Serializable {
            text: string;
            selected: boolean;
        }

    }

    export interface ActionCell extends Cell<Action[], ActionCell> {
        type: "ActionCell";
        value: Action[];
    }

    export interface Cell<V, SUB> {
        type: "ActionCell" | "NumericCell" | "TextCell";
        value: V;
        label: string;
        style: string;
        icon: Icon;
    }

    export interface Icon {
        code: string;
        placement: Icon.Placement;
        color: string;
        label: string;
    }

    export interface IconBuilder {
    }

    export interface NumericCell extends Cell<number, NumericCell> {
        type: "NumericCell";
        value: number;
        format: NumericFormat;
    }

    export interface TextCell extends Cell<string, TextCell> {
        type: "TextCell";
        value: string;
    }

    export interface Titlebar extends Widget<VoidWidgetData, Titlebar> {
        type: "Titlebar";
        data: VoidWidgetData;
        appTitle: string;
        version: string;
        buildTime: string;
        menu: Menu;
        actions: Action[];
    }

    export namespace DataRetrievalParams {

        export interface Page {
            index: number;
            size: number;
        }

    }

    export namespace DataRetrievalParams {

        export interface Sort {
            column: string;
            direction: Direction;
        }

    }

    export namespace DataRetrievalParams {

        export interface Filter {
            column: string;
            operator: ComparisonOperator;
            valueSet: DataRetrievalParams.Filter.Item[];
        }

    }

    export namespace Menu {

        export interface Item {
            id: string;
            label: string;
            icon: string;
            submenu: Menu;
        }

    }

    export namespace RowSelectParams {

        export interface Row<T> extends Serializable {
            id: T;
            selected: boolean;
        }

    }

    export interface Serializable {
    }

    export interface WidgetData extends Serializable {
        type: "WidgetList.Data" | "DateRangeInput.Data" | "Select.Data" | "Table.Data" | "VoidWidgetData" | "Value" | "FormattedText.Data" | "Autocomplete.Data" | "DateInput.Data" | "DateTimeInput.Data" | "GenericAutocomplete.Data" | "NumericInput.Data" | "TextInput.Data";
    }

    export interface VoidWidgetData extends WidgetData {
        type: "VoidWidgetData";
    }

    export interface Widget<WD, SUB> {
        type: "Button" | "WidgetList" | "FormattedText" | "Table" | "Titlebar" | "Autocomplete" | "DateInput" | "DateRangeInput" | "DateTimeInput" | "GenericAutocomplete" | "NumericInput" | "Select" | "TextInput";
        id: string;
        label: string;
        hint: string;
        enabled: boolean;
        data: WD;
    }

    export interface Validator {
        type: "Email" | "Max" | "Min" | "Pattern" | "Required" | "Size";
        message: string;
    }

    export interface NumericFormat extends AbstractFormat {
        precision: number;
        prefix: string;
        suffix: string;
        digitGrouping: boolean;
    }

    export interface BiFunction<T, U, R> {
    }

    export namespace DataRetrievalParams.Filter {

        export interface Item {
            text: string | null;
        }

    }

    export interface Value<V> extends WidgetData {
        type: "Value" | "FormattedText.Data" | "Autocomplete.Data" | "DateInput.Data" | "DateTimeInput.Data" | "GenericAutocomplete.Data" | "NumericInput.Data" | "TextInput.Data";
        value: V | null;
    }

    export interface Comparable<T> {
    }

    export interface Input<WD, V, SUB> extends Widget<WD, SUB> {
        type: "Autocomplete" | "DateInput" | "DateRangeInput" | "DateTimeInput" | "GenericAutocomplete" | "NumericInput" | "Select" | "TextInput";
        placeholder: string;
        validators: Validator[];
    }

    export interface AbstractFormat {
        validators: Validator[];
    }

    export type ComparisonOperator = "=" | "<>" | ">" | ">=" | "<" | "<=" | "IN" | "BETWEEN" | "LIKE";

    export type Direction = "ASC" | "DESC" | "UNDEFINED";

    export namespace Button {

        export type Style = "NONE" | "PRIMARY" | "ACCENT" | "WARN";

    }

    export namespace Action {

        export type Style = "ICON" | "BUTTON" | "ACTION_GROUP" | "LINK";

    }

    export type TextPlacement = "HIDDEN" | "BEFORE" | "AFTER";

    export namespace Column {

        export type Size = "AUTO" | "XS" | "S" | "M" | "L" | "XL" | "TIMESTAMP" | "NUMBER";

    }

    export namespace Column {

        export type TextAlign = "LEFT" | "CENTER" | "RIGHT";

    }

    export namespace ColumnGroup {

        export type TextAlign = "LEFT" | "CENTER" | "RIGHT";

    }

    export type Type = "TEXT" | "NUMBER" | "DATE" | "DATETIME" | "BOOLEAN";

    export namespace Table {

        export type SelectMode = "NONE" | "SINGLE" | "MULTI" | "CHECKBOX";

    }

    export namespace Icon {

        export type Placement = "BEFORE" | "AFTER";

    }

    export namespace RowSelectParams {

        export type SelectMode = "ALL_CHECKED" | "ALL_UNCHECKED" | "SINGLE";

    }

    export type ActionTriggerMode = "ALL_SELECTED" | "SINGLE";

}
