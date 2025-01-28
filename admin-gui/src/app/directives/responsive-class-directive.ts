/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Directive, HostBinding, Input, OnChanges, SimpleChanges} from '@angular/core';
import {DeviceTypeService} from '../services/device-type.service';

@Directive({
    selector: '[responsiveClass]',
    standalone: true
})
export class ResponsiveClassDirective implements OnChanges
{
    // tslint:disable-next-line:variable-name
    protected _elementClass = '';

    @Input()
    responsiveClass!: string;

    @HostBinding('class')
    get elementClass(): string
    {
        return this.getDeviceDependentClass(this._elementClass);
    }

    set elementClass(val: string)
    {
        this._elementClass = val;
    }

    constructor(public deviceTypeService: DeviceTypeService)
    {
    }

    ngOnChanges(changes: SimpleChanges): void
    {
        this._elementClass = this.responsiveClass;
    }

    private getDeviceDependentClass(input: string): string
    {
        const prefixes: string[] = input.split(' ');
        const deviceType = this.deviceTypeService.deviceType;
        const classNames: string[] = [];
        // the class itself
        prefixes.forEach(prefix =>
        {
            classNames.push(prefix);
        });
        // device type
        classNames.push(deviceType);
        if (deviceType === 'Phone' || deviceType === 'Tablet')
        {
            classNames.push('mobile');
        }
        // orientation
        classNames.push(this.deviceTypeService.orientation === 'Portrait' ? 'portrait' : 'landscape');
        const s = classNames.join(' ').toLowerCase();
        //console.log(`input: ${input} => ${s}`);
        return s;
    }
}
